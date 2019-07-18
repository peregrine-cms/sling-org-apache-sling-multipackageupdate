package com.headwire.sling.multipackageupdate;

import org.junit.Assert;
import org.junit.Test;

public final class MultiPackageUpdateResponseTest {

    private static final String STATUS = "example status";
    private static final String LOG = "example log";

    private final MultiPackageUpdateResponse model = new MultiPackageUpdateResponse(STATUS);

    @Test
    public void getStatus() {
        Assert.assertEquals(STATUS, model.getStatus());
    }

    @Test
    public void getLog() {
        model.setLog(LOG);
        Assert.assertEquals(LOG, model.getLog());
    }

}