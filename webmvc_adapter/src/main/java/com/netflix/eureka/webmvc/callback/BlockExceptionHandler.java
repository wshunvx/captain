package com.netflix.eureka.webmvc.callback;

import com.alibaba.csp.sentinel.slots.block.BlockException;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BlockExceptionHandler {

	public void handle(HttpServletRequest request, HttpServletResponse response, BlockException e) throws Exception {
        // Return 429 (Too Many Requests) by default.
        response.setStatus(429);
        PrintWriter out = response.getWriter();
        out.print("Blocked by Security (flow limiting)");
        out.flush();
        out.close();
    }

}
