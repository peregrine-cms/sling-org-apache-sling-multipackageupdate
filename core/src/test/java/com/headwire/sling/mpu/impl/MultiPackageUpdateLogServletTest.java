package com.headwire.sling.mpu.impl;

import com.headwire.sling.mpu.MultiPackageUpdate;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public final class MultiPackageUpdateLogServletTest extends MultiPackageUpdateActionServletTest<MultiPackageUpdateLogServlet> {

    public MultiPackageUpdateLogServletTest() {
        super(new MultiPackageUpdateLogServlet());
    }

    @Override
    protected void verifyAction(final MultiPackageUpdate verifier) {
        verifier.getLastLogText();
    }
}