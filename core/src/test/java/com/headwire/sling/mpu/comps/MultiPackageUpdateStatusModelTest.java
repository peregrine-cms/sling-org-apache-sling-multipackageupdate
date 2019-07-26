package com.headwire.sling.mpu.comps;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MultiPackageUpdateStatusModelTest extends MultiPackageUpdateModelTest<MultiPackageUpdateStatusModel> {

    public MultiPackageUpdateStatusModelTest() {
        super(new MultiPackageUpdateStatusModel());
    }

    protected void setUpImpl() {
        when(updater.getCurrentStatus())
                .thenReturn(mpuResponse);
    }

    protected void verifyExecuteImpl() {
        verify(updater).getCurrentStatus();
    }
}
