package com.headwire.sling.multipackageupdate.impl;

public final class MultiPackageUpdateResponse {

    private String status;
    private String log;

    public MultiPackageUpdateResponse(final String status) {
        this.status = status;
    }

    public void setLog(final String log) {
        this.log = log;
    }

    public String getStatus() {
        return status;
    }

    public String getLog() {
        return log;
    }
}