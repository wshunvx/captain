package com.netflix.eureka.http.handler;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.EntranceNode;
import com.alibaba.csp.sentinel.node.Node;
import com.netflix.eureka.command.CommandResponse;

/**
 * get metrics in tree mode, use id to specify detailed tree root
 * @author WX
 *
 */
@Endpoint(id = "tree")
public class FetchTreeCommandHandler {

    @ReadOperation
    public CommandResponse<String> handle(String id) {

        StringBuilder sb = new StringBuilder();

        DefaultNode start = Constants.ROOT;

        if (id == null) {
            visitTree(0, start, sb);
        } else {
            boolean exactly = false;
            for (Node n : start.getChildList()) {
                DefaultNode dn = (DefaultNode)n;
                if (dn.getId().getName().equals(id)) {
                    visitTree(0, dn, sb);
                    exactly = true;
                    break;
                }
            }

            if (!exactly) {
                for (Node n : start.getChildList()) {
                    DefaultNode dn = (DefaultNode)n;
                    if (dn.getId().getName().contains(id)) {
                        visitTree(0, dn, sb);
                    }
                }
            }
        }
        sb.append("\r\n\r\n");
        sb.append(
            "t:threadNum  pq:passQps  bq:blockQps  tq:totalQps  rt:averageRt  prq: passRequestQps 1mp:1m-pass "
                + "1mb:1m-block 1mt:1m-total").append("\r\n");
        return CommandResponse.ofSuccess(sb.toString());
    }

    private void visitTree(int level, DefaultNode node, /*@NonNull*/ StringBuilder sb) {
        for (int i = 0; i < level; ++i) {
            sb.append("-");
        }
        if (!(node instanceof EntranceNode)) {
            sb.append(String.format("%s(t:%s pq:%s bq:%s tq:%s rt:%s prq:%s 1mp:%s 1mb:%s 1mt:%s)",
                node.getId().getShowName(), node.curThreadNum(), node.passQps(),
                node.blockQps(), node.totalQps(), node.avgRt(), node.successQps(),
                node.totalRequest() - node.blockRequest(), node.blockRequest(),
                node.totalRequest())).append("\n");
        } else {
            sb.append(String.format("EntranceNode: %s(t:%s pq:%s bq:%s tq:%s rt:%s prq:%s 1mp:%s 1mb:%s 1mt:%s)",
                node.getId().getShowName(), node.curThreadNum(), node.passQps(),
                node.blockQps(), node.totalQps(), node.avgRt(), node.successQps(),
                node.totalRequest() - node.blockRequest(), node.blockRequest(),
                node.totalRequest())).append("\n");
        }
        for (Node n : node.getChildList()) {
            DefaultNode dn = (DefaultNode)n;
            visitTree(level + 1, dn, sb);
        }
    }
}
