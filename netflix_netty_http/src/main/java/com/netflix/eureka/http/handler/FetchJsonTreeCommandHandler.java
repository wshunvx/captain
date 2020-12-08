package com.netflix.eureka.http.handler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.Node;
import com.netflix.eureka.command.CommandResponse;
import com.netflix.eureka.command.vo.NodeVo;

/**
 * get tree node VO start from root node
 * @author WX
 *
 */
@Endpoint(id = "jsonTree")
public class FetchJsonTreeCommandHandler {

    @ReadOperation
    public CommandResponse<List<NodeVo>> handle() {
        List<NodeVo> results = new ArrayList<NodeVo>();
        visit(Constants.ROOT, results, null);
        return CommandResponse.ofSuccess(results);
    }

    /**
     * Preorder traversal.
     */
    private void visit(DefaultNode node, List<NodeVo> results, String parentId) {
        NodeVo vo = NodeVo.fromDefaultNode(node, parentId);
        results.add(vo);
        String id = vo.getId();
        for (Node n : node.getChildList()) {
            visit((DefaultNode)n, results, id);
        }
    }
}
