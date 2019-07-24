package com.headwire.sling.mpu.impl;

import org.junit.Assert;
import org.junit.Test;

public final class MultiPackageUpdateResponseImplTest {

    private static final String STATUS = "example status";
    private static final String LOG = "example log";

    private final MultiPackageUpdateResponseImpl model = new MultiPackageUpdateResponseImpl(STATUS);

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