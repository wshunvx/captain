package com.netflix.eureka.http.handler;

import java.util.Map.Entry;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.netflix.eureka.command.CommandResponse;

/**
 * get clusterNode metrics by id, request param: id={resourceName}
 * @author WX
 *
 */
@Endpoint(id = "cnode")
public class FetchClusterNodeHumanCommandHandler {

    private final static String FORMAT = "%-4s%-80s%-10s%-10s%-10s%-11s%-9s%-6s%-10s%-11s%-9s%-11s";
    private final static int MAX_LEN = 79;

    @ReadOperation
    public CommandResponse<String> handle(String id) {
        if (StringUtil.isEmpty(id)) {
            return CommandResponse.ofFailure("Invalid parameter: empty clusterNode name");
        }

        StringBuilder sb = new StringBuilder();

        int i = 0;
        int nameLength = 0;
        for (Entry<ResourceWrapper, ClusterNode> e : ClusterBuilderSlot.getClusterNodeMap().entrySet()) {
            if (e.getKey().getName().contains(id)) {
                int l = e.getKey().getShowName().length();
                if (l > nameLength) {
                    nameLength = l;
                }
                if (++i == 30) {
                    break;
                }
            }

        }
        nameLength = nameLength > MAX_LEN ? MAX_LEN : nameLength;
        String format = FORMAT.replaceAll("80", String.valueOf(nameLength + 1));

        sb.append(String.format(format, "idx", "id", "thread", "pass", "blocked", "success", "total", "aRt",
            "1m-pass", "1m-block", "1m-all", "exception")).append("\n");
        for (Entry<ResourceWrapper, ClusterNode> e : ClusterBuilderSlot.getClusterNodeMap().entrySet()) {
            if (e.getKey().getName().contains(id)) {
                ClusterNode node = e.getValue();
                String name = e.getKey().getShowName();
                int lenNum = (int)Math.ceil((double)name.length() / nameLength) - 1;

                sb.append(String.format(format, i + 1, lenNum == 0 ? name : name.substring(0, nameLength),
                    node.curThreadNum(), node.passQps(), node.blockQps(), node.successQps(), node.totalQps(),
                    node.avgRt(), node.totalRequest() - node.blockRequest(), node.blockRequest(),
                    node.totalRequest(), node.exceptionQps())).append("\n");
                for (int j = 1; j <= lenNum; ++j) {
                    int start = nameLength * j;
                    int end = j == lenNum ? name.length() : nameLength * (j + 1);
                    sb.append(String.format(format, "", name.substring(start, end), "", "", "", "", "", "", "", "", "",
                        "", "", "")).append("\n");
                }

                if (++i == 30) {
                    break;
                }
            }
        }

        return CommandResponse.ofSuccess(sb.toString());
    }
}
