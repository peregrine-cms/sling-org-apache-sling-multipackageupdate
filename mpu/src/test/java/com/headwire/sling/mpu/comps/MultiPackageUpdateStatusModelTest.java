package com.headwire.sling.mpu.comps;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.headwire.sling.mpu.impl.MultiPackageUpdateServiceTest.TEST_CONFIG_NAME;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MultiPackageUpdateStatusModelTest extends MultiPackageUpdateModelTest<MultiPackageUpdateStatusModel> {

    public MultiPackageUpdateStatusModelTest() {
        super(new MultiPackageUpdateStatusModel());
    }

    protected void setUpImpl() {
        when(updater.getStatus(TEST_CONFIG_NAME))
                .thenReturn(mpuResponse);
    }

    protected void verifyExecuteImpl() {
        verify(updater).getStatus(eq(TEST_CONFIG_NAME));
    }
}
