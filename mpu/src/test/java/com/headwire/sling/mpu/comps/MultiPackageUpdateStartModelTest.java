package com.headwire.sling.mpu.comps;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.headwire.sling.mpu.impl.MultiPackageUpdateServiceTest.TEST_CONFIG_NAME;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MultiPackageUpdateStartModelTest extends MultiPackageUpdateModelTest<MultiPackageUpdateStartModel> {

    public MultiPackageUpdateStartModelTest() {
        super(new MultiPackageUpdateStartModel());
    }

    protected void setUpImpl() {
        when(updater.start(TEST_CONFIG_NAME))
                .thenReturn(mpuResponse);
    }

    protected void verifyExecuteImpl() {
        verify(updater).start(eq(TEST_CONFIG_NAME));
    }
}
