package com.headwire.sling.mpu.impl;

import com.headwire.sling.mpu.MultiPackageUpdate;
import com.headwire.sling.mpu.MultiPackageUpdateResponse.Code;
import junitx.util.PrivateAccessor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.headwire.sling.mpu.impl.MultiPackageUpdateServiceTest.TEST_CONFIG_NAME;
import static org.mockito.Mockito.verify;

public abstract class MultiPackageUpdateActionServletTest<ServletType extends MultiPackageUpdateServlet> {

    private final ServletType model;

    @Mock
    private MultiPackageUpdate updater;

    public MultiPackageUpdateActionServletTest(final ServletType model) {
        this.model = model;
    }

    @Before
    public final void setUp() throws NoSuchFieldException {
        PrivateAccessor.setField(model, "updater", updater);
        PrivateAccessor.setField(model, "httpMapper", new HttpStatusCodeMapperService());
    }

    @Test
    public final void execute() {
        model.execute(TEST_CONFIG_NAME);
        verifyAction(verify(updater));
    }

    @Test
    public final void getStatusCode() {
        for (final Code code : Code.class.getEnumConstants()) {
            Assert.assertNotNull(model.getStatusCode(code));
        }
    }

    protected abstract void verifyAction(final MultiPackageUpdate verifier);
}