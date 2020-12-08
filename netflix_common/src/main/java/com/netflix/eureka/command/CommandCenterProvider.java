package com.netflix.eureka.command;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.SpiLoader;
import com.netflix.eureka.transport.CommandCenter;

public final class CommandCenterProvider {

    private static CommandCenter commandCenter = null;

    static {
        resolveInstance();
    }

    private static void resolveInstance() {
        CommandCenter resolveCommandCenter = SpiLoader.loadHighestPriorityInstance(CommandCenter.class);

        if (resolveCommandCenter == null) {
            RecordLog.warn("[CommandCenterProvider] WARN: No existing CommandCenter found");
        } else {
            commandCenter = resolveCommandCenter;
            RecordLog.info("[CommandCenterProvider] CommandCenter resolved: {}", resolveCommandCenter.getClass()
                .getCanonicalName());
        }
    }

    /**
     * Get resolved {@link CommandCenter} instance.
     *
     * @return resolved {@code CommandCenter} instance
     */
    public static CommandCenter getCommandCenter() {
        return commandCenter;
    }

    private CommandCenterProvider() {}
}
