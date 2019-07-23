package com.headwire.sling.multipackageupdate.impl;

import com.headwire.sling.multipackageupdate.MultiPackageUpdate;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public final class MultiPackageUpdateStatusServletTest extends MultiPackageUpdateActionServletTest<MultiPackageUpdateStatusServlet> {

    public MultiPackageUpdateStatusServletTest() {
        super(new MultiPackageUpdateStatusServlet());
    }

    @Override
    protected void verifyAction(final MultiPackageUpdate verifier) {
        verifier.getCurrentStatus();
    }
}