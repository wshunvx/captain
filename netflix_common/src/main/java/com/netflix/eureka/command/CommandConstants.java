package com.netflix.eureka.command;

public final class CommandConstants {

    public static final String VERSION_COMMAND = "version";

    public static final String MSG_INVALID_COMMAND = "Invalid command";
    public static final String MSG_UNKNOWN_COMMAND_PREFIX = "Unknown command";

    public static final String MSG_SUCCESS = "success";
    public static final String MSG_FAIL = "failed";

    public static final String APP_TYPE_GATEWAY = "100";

    public static final int STRIP_PREFIX_ROUTE_TRUE = 0;
    public static final int STRIP_PREFIX_ROUTE_FALSE = 1;
    
    public static final int RESOURCE_MODE_ROUTE_ID = 0;
    public static final int RESOURCE_MODE_CUSTOM_API_NAME = 1;

    public static final int PARAM_PARSE_STRATEGY_CLIENT_IP = 0;
    public static final int PARAM_PARSE_STRATEGY_HOST = 1;
    public static final int PARAM_PARSE_STRATEGY_HEADER = 2;
    public static final int PARAM_PARSE_STRATEGY_URL_PARAM = 3;
    public static final int PARAM_PARSE_STRATEGY_COOKIE = 4;

    public static final int URL_MATCH_STRATEGY_EXACT = 0;
    public static final int URL_MATCH_STRATEGY_PREFIX = 1;
    public static final int URL_MATCH_STRATEGY_REGEX = 2;

    public static final int PARAM_MATCH_STRATEGY_EXACT = 0;
    public static final int PARAM_MATCH_STRATEGY_PREFIX = 1;
    public static final int PARAM_MATCH_STRATEGY_REGEX = 2;
    public static final int PARAM_MATCH_STRATEGY_CONTAINS = 3;

    public static final String NETFLIX_CONTEXT_KEY = "_netflix_context";
    
    public static final String GATEWAY_CONTEXT_DEFAULT = "gateway_context_default";
    public static final String GATEWAY_CONTEXT_PREFIX = "gateway_context$$";
    public static final String GATEWAY_CONTEXT_ROUTE_PREFIX = "gateway_context$$route$$";

    public static final String GATEWAY_NOT_MATCH_PARAM = "$NM";
    public static final String GATEWAY_DEFAULT_PARAM = "$D";
    
    private CommandConstants() {}
}
