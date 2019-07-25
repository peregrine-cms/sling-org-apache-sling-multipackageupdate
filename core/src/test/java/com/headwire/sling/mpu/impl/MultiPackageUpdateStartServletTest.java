package com.headwire.sling.mpu.impl;

import com.headwire.sling.mpu.MultiPackageUpdate;
import com.headwire.sling.mpu.PackagesListEndpoint;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.*;

@RunWith(MockitoJUnitRunner.class)
public final class MultiPackageUpdateStartServletTest extends MultiPackageUpdateActionServletTest<MultiPackageUpdateStartServlet> {

    public MultiPackageUpdateStartServletTest() {
        super(new MultiPackageUpdateStartServlet());
    }

    @Override
    protected void verifyAction(final MultiPackageUpdate verifier) {
        verifier.start();
    }
}