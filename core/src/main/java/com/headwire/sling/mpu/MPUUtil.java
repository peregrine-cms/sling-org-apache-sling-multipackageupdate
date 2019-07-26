package com.headwire.sling.mpu;

public final class MPUUtil {

    public static final String DO_NOT_INSTANTIATE_UTIL_CLASS = "Util classes cannot be instantiated";

    public static final String EQUAL = "=";
    public static final String SLASH = "/";

    public static final String GET = "GET";
    public static final String POST = "POST";

    public static final String HTML = "html";
    public static final String JSON = "json";

    public static final String APPLICATION = "application";
    public static final String TEXT = "text";
    public static final String APPLICATION_JSON = APPLICATION + SLASH + JSON;
    public static final String TEXT_HTML = TEXT + SLASH + HTML;

    public static final String UTF_8 = "utf-8";

    public static final String ACCEPT = "Accept";

    public static final String COMPONENTS = "components";

    private MPUUtil() {
        throw new UnsupportedOperationException(DO_NOT_INSTANTIATE_UTIL_CLASS);
    }

}
