package com.headwire.sling.mpu.impl;

import com.headwire.sling.mpu.MultiPackageUpdate;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.headwire.sling.mpu.impl.MultiPackageUpdateServiceTest.TEST_CONFIG_NAME;

@RunWith(MockitoJUnitRunner.class)
public final class MultiPackageUpdateStartServletTest extends MultiPackageUpdateActionServletTest<MultiPackageUpdateStartServlet> {

    public MultiPackageUpdateStartServletTest() {
        super(new MultiPackageUpdateStartServlet());
    }

    @Override
    protected void verifyAction(final MultiPackageUpdate verifier) {
        verifier.start(TEST_CONFIG_NAME);
    }
}