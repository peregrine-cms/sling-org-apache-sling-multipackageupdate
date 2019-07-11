package com.headwire.sling.update;

public interface PackagesListEndpoint {

    String getServerUrl();

    String getFileUrl(final String name);

    String getPackagesListUrl();
}