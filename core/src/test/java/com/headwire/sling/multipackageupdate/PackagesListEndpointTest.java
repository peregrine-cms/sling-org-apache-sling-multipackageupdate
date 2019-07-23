package com.headwire.sling.multipackageupdate;

import org.junit.Assert;
import org.junit.Test;

public class PackagesListEndpointTest {

    public static final String SERVER_URL = "http://localhost:8080/content";
    public static final String PACKAGES_TXT = "packages.txt";
    private static final String SAMPLE_PACKAGE = "package-1.0.0.zip";
    private static final String HTTP_SAMPLE_PACKAGE = "http://localhost:8080/package-1.0.0.zip";
    private static final String HTTPS_SAMPLE_PACKAGE = "https://localhost:8080/package-1.0.0.zip";

    private final PackagesListEndpoint model = new PackagesListEndpoint(SERVER_URL, PACKAGES_TXT);

    @Test
    public void getServerUrl() {
        Assert.assertEquals(SERVER_URL, model.getServerUrl());
    }

    @Test
    public void getFileUrl() {
        Assert.assertEquals(SERVER_URL + "/" + SAMPLE_PACKAGE, model.getFileUrl(SAMPLE_PACKAGE));
        Assert.assertEquals(HTTP_SAMPLE_PACKAGE, HTTP_SAMPLE_PACKAGE);
        Assert.assertEquals(HTTPS_SAMPLE_PACKAGE, HTTPS_SAMPLE_PACKAGE);
    }

    @Test
    public void getPackagesListUrl() {
        Assert.assertEquals(SERVER_URL + "/" + PACKAGES_TXT, model.getPackagesListUrl());
    }
}