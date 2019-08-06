package com.headwire.sling.mpu.impl;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public final class MultiPackageUpdateResponseImplTest {

    private static final String STATUS = "example status";
    private static final String LOG = "example log";

    private final MultiPackageUpdateResponseImpl model = new MultiPackageUpdateResponseImpl();

    @Test
    public void getStatus() {
        model.setAction(STATUS);
        Assert.assertEquals(STATUS, model.getAction());
    }

    @Test
    public void getLog() {
        model.setDetails(Arrays.asList(new String[] { LOG }));
        Assert.assertEquals(LOG, model.getDetails().get(0));
    }

}