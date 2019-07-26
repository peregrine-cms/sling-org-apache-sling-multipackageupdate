package com.headwire.sling.mpu.comps;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MultiPackageUpdateStopModelTest extends MultiPackageUpdateModelTest<MultiPackageUpdateStopModel> {

    public MultiPackageUpdateStopModelTest() {
        super(new MultiPackageUpdateStopModel());
    }

    protected void setUpImpl() {
        when(updater.stop())
                .thenReturn(mpuResponse);
    }

    protected void verifyExecuteImpl() {
        verify(updater).stop();
    }
}
