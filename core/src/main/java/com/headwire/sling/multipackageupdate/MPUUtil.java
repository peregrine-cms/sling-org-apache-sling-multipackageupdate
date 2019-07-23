package com.headwire.sling.multipackageupdate;

public final class MPUUtil {

    public static final String DO_NOT_INSTANTIATE_UTIL_CLASS = "Util classes cannot be instantiated";

    public static final String EQUALS = "=";
    public static final String SLASH = "/";

    public static final String GET = "GET";
    public static final String POST = "POST";

    public static final String HTML = "html";
    public static final String JSON = "json";

    private MPUUtil() {
        throw new UnsupportedOperationException(DO_NOT_INSTANTIATE_UTIL_CLASS);
    }

}
