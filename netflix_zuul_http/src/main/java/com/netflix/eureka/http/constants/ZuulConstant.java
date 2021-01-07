package com.netflix.eureka.http.constants;

public class ZuulConstant {

    public final static String AUTHORITY_TYPE = "Authorization";
    public final static String AUTHORITY_TOKEN = "Atoken";
    public final static String AUTHORITY_SERVICE = "Svrid";
    public final static String AUTHORITY_TYPE_GROUP = "Group";
    public final static String AUTHORITY_TYPE_USER = "User";

    public final static Integer RESOURCE_TYPE_MENU = 1;
    public final static Integer RESOURCE_TYPE_BTN = 2;
    
    public static final String JWT_KEY_ID = "obj";
    public static final String JWT_KEY_METADATA = "metadata";
    public static final String JWT_KEY_AGENT = "agent";
    
    /**
     * Zuul use Security as default context when serviceId is empty.
     */
    public static final String ZUUL_DEFAULT_CONTEXT = "zuul_default_context";

    /**
     * Zuul context key for keeping Security entries.
     *
     * @since 1.6.0
     */
    public static final String ZUUL_CTX_SENTINEL_ENTRIES_KEY = "_security_entries";

}
