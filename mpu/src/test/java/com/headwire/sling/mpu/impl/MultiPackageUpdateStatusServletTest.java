package com.headwire.sling.mpu.impl;

import com.headwire.sling.mpu.MultiPackageUpdate;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.headwire.sling.mpu.impl.MultiPackageUpdateServiceTest.TEST_CONFIG_NAME;
import static org.mockito.Matchers.eq;

@RunWith(MockitoJUnitRunner.class)
public final class MultiPackageUpdateStatusServletTest extends MultiPackageUpdateActionServletTest<MultiPackageUpdateStatusServlet> {

    public MultiPackageUpdateStatusServletTest() {
        super(new MultiPackageUpdateStatusServlet());
    }

    @Override
    protected void verifyAction(final MultiPackageUpdate verifier) {
        verifier.getStatus(eq(TEST_CONFIG_NAME));
    }
}