package com.headwire.sling.mpu.comps;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.headwire.sling.mpu.impl.MultiPackageUpdateServiceTest.TEST_CONFIG_NAME;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MultiPackageUpdateStopModelTest extends MultiPackageUpdateModelTest<MultiPackageUpdateStopModel> {

    public MultiPackageUpdateStopModelTest() {
        super(new MultiPackageUpdateStopModel());
    }

    protected void setUpImpl() {
        when(updater.stop(TEST_CONFIG_NAME))
                .thenReturn(mpuResponse);
    }

    protected void verifyExecuteImpl() {
        verify(updater).stop(TEST_CONFIG_NAME);
    }
}
