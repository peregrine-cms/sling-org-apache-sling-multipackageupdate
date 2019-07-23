package com.headwire.sling.multipackageupdate.impl;

import com.headwire.sling.multipackageupdate.MultiPackageUpdate;
import com.headwire.sling.multipackageupdate.PackagesListEndpoint;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.*;

@RunWith(MockitoJUnitRunner.class)
public final class MultiPackageUpdateStartServletTest extends MultiPackageUpdateActionServletTest<MultiPackageUpdateStartServlet> {

    public MultiPackageUpdateStartServletTest() {
        super(new MultiPackageUpdateStartServlet());
    }

    @Mock
    private MultiPackageUpdateServletConfig config;

    @Override
    protected void setUpImpl(final MultiPackageUpdateStartServlet model) {
        model.activate(config);
    }

    @Override
    protected void verifyAction(final MultiPackageUpdate verifier) {
        verifier.start(any(PackagesListEndpoint.class), anyString(), anyInt());
    }
}