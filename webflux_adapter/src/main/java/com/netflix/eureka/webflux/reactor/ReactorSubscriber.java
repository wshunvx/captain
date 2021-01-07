package com.netflix.eureka.webflux.reactor;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alibaba.csp.sentinel.AsyncEntry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.function.Supplier;
import com.netflix.eureka.command.CommandConstants;

import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.publisher.Operators;
import reactor.core.publisher.SignalType;
import reactor.util.context.Context;

public class ReactorSubscriber<T> extends InheritableSubscriber implements CoreSubscriber<T> {

    private final EntryConfig entryConfig;

    private final CoreSubscriber<? super T> actual;
    private final boolean unary;

    private volatile AsyncEntry currentEntry;
    private final AtomicBoolean entryExited = new AtomicBoolean(false);

    public ReactorSubscriber(EntryConfig entryConfig,
                                     CoreSubscriber<? super T> actual,
                                     boolean unary) {
        checkEntryConfig(entryConfig);
        this.entryConfig = entryConfig;
        this.actual = actual;
        this.unary = unary;
    }

    private void checkEntryConfig(EntryConfig config) {
        AssertUtil.notNull(config, "entryConfig cannot be null");
    }

    @Override
    public Context currentContext() {
        if (currentEntry == null || entryExited.get()) {
            return actual.currentContext();
        }
        com.alibaba.csp.sentinel.context.Context sentinelContext = currentEntry.getAsyncContext();
        if (sentinelContext == null) {
            return actual.currentContext();
        }
        return actual.currentContext()
            .put(CommandConstants.NETFLIX_CONTEXT_KEY, currentEntry.getAsyncContext());
    }

    private void doWithContextOrCurrent(Supplier<Optional<com.alibaba.csp.sentinel.context.Context>> contextSupplier,
                                        Runnable f) {
        Optional<com.alibaba.csp.sentinel.context.Context> contextOpt = contextSupplier.get();
        if (!contextOpt.isPresent()) {
            // Provided context is absent, use current context.
            f.run();
        } else {
            // Run on provided context.
            ContextUtil.runOnContext(contextOpt.get(), f);
        }
    }

    @Override
	public void onNext(T t) {
    	Objects.requireNonNull(t, "onNext");
        try {
            hookOnNext(t);
        } catch (Throwable throwable) {
            onError(Operators.onOperatorError(subscription, throwable, t, currentContext()));
        }
		
	}

	@Override
	public void onError(Throwable t) {
		Objects.requireNonNull(t, "onError");

        if (S.getAndSet(this, Operators.cancelledSubscription()) == Operators
            .cancelledSubscription()) {
            // Already cancelled concurrently

            // Workaround for Sentinel BlockException:
            // Here we add a predicate method to decide whether exception should be dropped implicitly
            // or call the {@code onErrorDropped} hook.
            if (shouldCallErrorDropHook()) {
                Operators.onErrorDropped(t, currentContext());
            }

            return;
        }

        try {
            hookOnError(t);
        } catch (Throwable e) {
            e = Exceptions.addSuppressed(e, t);
            Operators.onErrorDropped(e, currentContext());
        } finally {
            safeHookFinally(SignalType.ON_ERROR);
        }
		
	}

	@Override
	public void onComplete() {
		if (S.getAndSet(this, Operators.cancelledSubscription()) != Operators
	            .cancelledSubscription()) {
	            //we're sure it has not been concurrently cancelled
	            try {
	                hookOnComplete();
	            } catch (Throwable throwable) {
	                //onError itself will short-circuit due to the CancelledSubscription being push above
	                hookOnError(Operators.onOperatorError(throwable, currentContext()));
	            } finally {
	                safeHookFinally(SignalType.ON_COMPLETE);
	            }
	        }
		
	}

	@Override
	public void onSubscribe(Subscription s) {
		if (Operators.setOnce(S, this, s)) {
            try {
                hookOnSubscribe(s);
            } catch (Throwable throwable) {
                onError(Operators.onOperatorError(s, throwable, currentContext()));
            }
        }
		
	}

	private void entryWhenSubscribed() {
        ContextConfig sentinelContextConfig = entryConfig.getContextConfig();
        if (sentinelContextConfig != null) {
            // If current we're already in a context, the context config won't work.
            ContextUtil.enter(sentinelContextConfig.getContextName(), sentinelContextConfig.getOrigin());
        }
        try {
            AsyncEntry entry = SphU.asyncEntry(entryConfig.getResourceName(), entryConfig.getResourceType(),
                entryConfig.getEntryType(), entryConfig.getAcquireCount(), entryConfig.getArgs());
            this.currentEntry = entry;
            actual.onSubscribe(this);
        } catch (BlockException ex) {
            // Mark as completed (exited) explicitly.
            entryExited.set(true);
            // Signal cancel and propagate the {@code BlockException}.
            cancel();
            actual.onSubscribe(this);
            actual.onError(ex);
        } finally {
            if (sentinelContextConfig != null) {
                ContextUtil.exit();
            }
        }
    }

    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        doWithContextOrCurrent(() -> currentContext().getOrEmpty(CommandConstants.NETFLIX_CONTEXT_KEY),
            this::entryWhenSubscribed);
    }

    protected void hookOnNext(T value) {
        if (isDisposed()) {
            tryCompleteEntry();
            return;
        }
        doWithContextOrCurrent(() -> Optional.ofNullable(currentEntry).map(AsyncEntry::getAsyncContext),
            () -> actual.onNext(value));

        if (unary) {
            // For some cases of unary operator (Mono), we have to do this during onNext hook.
            // e.g. this kind of order: onSubscribe() -> onNext() -> cancel() -> onComplete()
            // the onComplete hook will not be executed so we'll need to complete the entry in advance.
            tryCompleteEntry();
        }
    }

    protected void hookOnComplete() {
        tryCompleteEntry();
        actual.onComplete();
    }

    protected boolean shouldCallErrorDropHook() {
        // When flow control triggered or stream terminated, the incoming
        // deprecated exceptions should be dropped implicitly, so we'll not call the `onErrorDropped` hook.
        return !entryExited.get();
    }

    @Override
    protected void hookOnError(Throwable t) {
        if (currentEntry != null && currentEntry.getAsyncContext() != null) {
            // Normal requests with non-BlockException will go through here.
            Tracer.traceContext(t, currentEntry.getAsyncContext());
        }
        tryCompleteEntry();
        actual.onError(t);
    }

    @Override
    protected void hookOnCancel() {
        tryCompleteEntry();
    }

    private boolean tryCompleteEntry() {
        if (currentEntry != null && entryExited.compareAndSet(false, true)) {
            currentEntry.exit(1, entryConfig.getArgs());
            return true;
        }
        return false;
    }

	@Override
	protected void hookFinally(SignalType type) {
		// TODO Auto-generated method stub
		
	}
}
