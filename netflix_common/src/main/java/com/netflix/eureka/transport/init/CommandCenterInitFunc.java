package com.netflix.eureka.transport.init;

import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.init.InitOrder;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.netflix.eureka.command.CommandCenterProvider;
import com.netflix.eureka.transport.CommandCenter;

@InitOrder(-1)
public class CommandCenterInitFunc implements InitFunc {

    @Override
    public void init() throws Exception {
        CommandCenter commandCenter = CommandCenterProvider.getCommandCenter();

        if (commandCenter == null) {
            RecordLog.warn("[CommandCenterInitFunc] Cannot resolve CommandCenter");
            return;
        }

        commandCenter.beforeStart();
        commandCenter.start();
        RecordLog.info("[CommandCenterInit] Starting command center: "
                + commandCenter.getClass().getCanonicalName());
    }
}
