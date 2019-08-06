package com.headwire.sling.mpu.comps;

import com.headwire.sling.mpu.HttpStatusCodeMapper;
import com.headwire.sling.mpu.MultiPackageUpdate;
import com.headwire.sling.mpu.MultiPackageUpdateResponse;
import com.headwire.sling.mpu.MultiPackageUpdate.Operation;
import com.headwire.sling.mpu.MultiPackageUpdateResponse.Code;
import junitx.util.PrivateAccessor;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestPathInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.servlet.http.HttpServletResponse;

import java.lang.reflect.Parameter;

import static com.headwire.sling.mpu.impl.MultiPackageUpdateServiceTest.TEST_CONFIG_NAME;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class MultiPackageUpdateModelTest<ModelType extends MultiPackageUpdateModel> {

    private final ModelType model;

    @Mock
    protected MultiPackageUpdate updater;

    @Mock
    private HttpStatusCodeMapper httpMapper;

    @Mock
    private SlingHttpServletRequest request;

    @Mock
    private RequestPathInfo requestPathInfo;

    @Mock
    private SlingHttpServletResponse response;

    @Mock
    protected MultiPackageUpdateResponse mpuResponse;

    protected MultiPackageUpdateModelTest(final ModelType model) {
        this.model = model;
    }

    @Before
    public void setUp() throws NoSuchFieldException {
        PrivateAccessor.setField(model, "updater", updater);
        PrivateAccessor.setField(model, "httpMapper", httpMapper);
        PrivateAccessor.setField(model, "request", request);
        PrivateAccessor.setField(model, "response", response);

        when(request.getRequestPathInfo()).thenReturn(requestPathInfo);
        when(requestPathInfo.getSuffix()).thenReturn(TEST_CONFIG_NAME);
        when(httpMapper.getStatusCode(any(Operation.class), any(Code.class)))
                .thenReturn(HttpServletResponse.SC_OK);
        setUpImpl();
    }

    @Test
    public final void execute() {
        Assert.assertNotNull(model.execute());
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verifyExecuteImpl();
    }

    protected abstract void setUpImpl();

    protected abstract void verifyExecuteImpl();
}
