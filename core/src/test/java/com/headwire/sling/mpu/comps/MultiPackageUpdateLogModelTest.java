package com.headwire.sling.mpu.comps;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MultiPackageUpdateLogModelTest extends MultiPackageUpdateModelTest<MultiPackageUpdateLogModel> {

    public MultiPackageUpdateLogModelTest() {
        super(new MultiPackageUpdateLogModel());
    }

    protected void setUpImpl() {
        when(updater.getLastLogText())
                .thenReturn(mpuResponse);
    }

    protected void verifyExecuteImpl() {
        verify(updater).getLastLogText();
    }
}
