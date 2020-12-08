package com.netflix.eureka.http.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import com.netflix.eureka.command.CommandResponse;
import com.netflix.eureka.command.vo.NodeVo;

/**
 * get all clusterNode VO, use type=notZero to ignore those nodes with totalRequest <=0
 * @author WX
 *
 */
@Endpoint(id = "clusterNode")
public class FetchSimpleClusterNodeCommandHandler {

    @ReadOperation
    public CommandResponse<List<NodeVo>> handle(String type) {
        /*
         * type==notZero means nodes whose totalRequest <= 0 will be ignored.
         */
        List<NodeVo> list = new ArrayList<NodeVo>();
        Map<ResourceWrapper, ClusterNode> map = ClusterBuilderSlot.getClusterNodeMap();
        if (map == null) {
            return CommandResponse.ofSuccess(list);
        }
        for (Map.Entry<ResourceWrapper, ClusterNode> entry : map.entrySet()) {
            if ("notZero".equalsIgnoreCase(type)) {
                if (entry.getValue().totalRequest() > 0) {
                    list.add(NodeVo.fromClusterNode(entry.getKey(), entry.getValue()));
                }
            } else {
                list.add(NodeVo.fromClusterNode(entry.getKey(), entry.getValue()));
            }
        }
        return CommandResponse.ofSuccess(list);
    }

}
