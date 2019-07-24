package com.headwire.sling.mpu.impl;

import com.headwire.sling.mpu.MultiPackageUpdate;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public final class MultiPackageUpdateStopServletTest extends MultiPackageUpdateActionServletTest<MultiPackageUpdateStopServlet> {

    public MultiPackageUpdateStopServletTest() {
        super(new MultiPackageUpdateStopServlet());
    }

    @Override
    protected void verifyAction(final MultiPackageUpdate verifier) {
        verifier.stop();
    }
}