package com.netflix.eureka.http.handler;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.netflix.eureka.command.CommandResponse;
import com.netflix.eureka.command.vo.NodeVo;
import com.netflix.eureka.gson.JSONFormatter;

/**
 * get clusterNode VO by id, request param: id={resourceName}
 * @author WX
 *
 */
@Endpoint(id = "clusterNodeById")
public class FetchClusterNodeByIdCommandHandler {

    @ReadOperation
    public CommandResponse<String> handle(String id) {
        if (StringUtil.isEmpty(id)) {
            return CommandResponse.ofFailure("Invalid parameter: empty clusterNode name");
        }
        ClusterNode node = ClusterBuilderSlot.getClusterNode(id);
        if (node != null) {
            return CommandResponse.ofSuccess(JSONFormatter.toJSON(NodeVo.fromClusterNode(id, node)));
        } else {
            return CommandResponse.ofSuccess("{}");
        }
    }
}
