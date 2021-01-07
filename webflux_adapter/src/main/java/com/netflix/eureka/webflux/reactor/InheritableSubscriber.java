package com.netflix.eureka.webflux.reactor;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.publisher.Operators;
import reactor.core.publisher.SignalType;
import reactor.util.context.Context;

abstract class InheritableSubscriber implements Subscription, Disposable {

    volatile Subscription subscription;

    static AtomicReferenceFieldUpdater<InheritableSubscriber, Subscription> S =
        AtomicReferenceFieldUpdater.newUpdater(InheritableSubscriber.class, Subscription.class,
            "subscription");

    /**
     * Return current {@link Subscription}
     *
     * @return current {@link Subscription}
     */
    protected Subscription upstream() {
        return subscription;
    }

    @Override
    public boolean isDisposed() {
        return subscription == Operators.cancelledSubscription();
    }

    /**
     * {@link Disposable#dispose() Dispose} the {@link Subscription} by
     * {@link Subscription#cancel() cancelling} it.
     */
    @Override
    public void dispose() {
        cancel();
    }

    protected void hookOnSubscribe(Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    protected abstract Context currentContext();
    
    protected abstract void hookOnCancel();

    protected abstract void hookFinally(SignalType type);

    protected void hookOnError(Throwable throwable) {
        throw Exceptions.errorCallbackNotImplemented(throwable);
    }

    @Override
    public final void request(long n) {
        if (Operators.validate(n)) {
            Subscription s = this.subscription;
            if (s != null) {
                s.request(n);
            }
        }
    }

    /**
     * {@link #request(long) Request} an unbounded amount.
     */
    public final void requestUnbounded() {
        request(Long.MAX_VALUE);
    }

    @Override
    public final void cancel() {
        if (Operators.terminate(S, this)) {
            try {
                hookOnCancel();
            } catch (Throwable throwable) {
                hookOnError(Operators.onOperatorError(subscription, throwable, currentContext()));
            } finally {
                safeHookFinally(SignalType.CANCEL);
            }
        }
    }

    void safeHookFinally(SignalType type) {
        try {
            hookFinally(type);
        } catch (Throwable finallyFailure) {
            Operators.onErrorDropped(finallyFailure, currentContext());
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
