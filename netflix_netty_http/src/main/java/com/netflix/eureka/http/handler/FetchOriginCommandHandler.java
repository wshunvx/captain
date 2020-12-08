package com.netflix.eureka.http.handler;

import java.util.Map.Entry;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.StatisticNode;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import com.netflix.eureka.command.CommandResponse;

/**
 * get origin clusterNode by id, request param: id={resourceName}
 * @author WX
 *
 */
@Endpoint(id = "origin")
public class FetchOriginCommandHandler {

    private final static String FORMAT = "%-4s%-80s%-10s%-10s%-11s%-9s%-6s%-10s%-11s%-9s";
    private final static int MAX_LEN = 79;

    @ReadOperation
    public CommandResponse<String> handle(String id) {
        StringBuilder sb = new StringBuilder();

        ClusterNode cNode = null;

        boolean exactly = false;
        for (Entry<ResourceWrapper, ClusterNode> e : ClusterBuilderSlot.getClusterNodeMap().entrySet()) {
            if (e.getKey().getName().equals(id)) {
                cNode = e.getValue();
                sb.append("id: ").append(e.getKey().getShowName()).append("\n");
                sb.append("\n");
                exactly = true;
                break;
            }
        }

        if (!exactly) {
            for (Entry<ResourceWrapper, ClusterNode> e : ClusterBuilderSlot.getClusterNodeMap().entrySet()) {
                if (e.getKey().getName().indexOf(id) > 0) {
                    cNode = e.getValue();
                    sb.append("id: ").append(e.getKey().getShowName()).append("\n");
                    sb.append("\n");
                    break;
                }
            }
        }

        if (cNode == null) {
            return CommandResponse.ofSuccess("Not find cNode with id " + id);
        }
        int i = 0;
        int nameLength = 0;
        for (Entry<String, StatisticNode> e : cNode.getOriginCountMap().entrySet()) {
            int l = e.getKey().length();
            if (l > nameLength) {
                nameLength = l;
            }
            if (++i == 120) {
                break;
            }
        }
        nameLength = nameLength > MAX_LEN ? MAX_LEN : nameLength;
        String format = FORMAT.replaceAll("80", String.valueOf(nameLength + 1));
        i = 0;
        sb.append(String
            .format(format, "idx", "origin", "threadNum", "passQps", "blockQps", "totalQps", "aRt", "1m-pass",
                "1m-block", "1m-total")).append("\n");

        for (Entry<String, StatisticNode> e : cNode.getOriginCountMap().entrySet()) {
            StatisticNode node = e.getValue();
            String key = e.getKey();
            double lenNum = Math.ceil(key.length() / nameLength) - 1;
            sb.append(String
                .format(format, i + 1, lenNum == 0 ? key : key.substring(0, nameLength), node.curThreadNum(),
                    node.passQps(), node.blockQps(), node.totalQps(), node.avgRt(),
                    node.totalRequest() - node.blockRequest(), node.blockRequest(), node.totalRequest()))
                .append("\n");
            for (int j = 1; j <= lenNum; ++j) {
                int start = nameLength * j;
                int end = j == lenNum ? key.length() : nameLength * (j + 1);
                sb.append(String
                    .format(format, "", key.substring(start, end), "", "", "", "", "", "", "", "", "", "", "", ""))
                    .append("\n");
            }
            if (++i == 30) {
                break;
            }

        }

        return CommandResponse.ofSuccess(sb.toString());
    }
}
