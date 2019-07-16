package com.headwire.sling.multipackageupdate;

public interface PackagesListEndpoint {

    String getServerUrl();

    String getFileUrl(final String name);

    String getPackagesListUrl();
}