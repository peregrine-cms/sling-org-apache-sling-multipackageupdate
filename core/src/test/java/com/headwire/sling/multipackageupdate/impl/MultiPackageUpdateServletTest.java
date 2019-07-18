package com.headwire.sling.multipackageupdate.impl;

import com.headwire.sling.multipackageupdate.MultiPackageUpdate;
import com.headwire.sling.multipackageupdate.PackagesListEndpoint;
import junitx.util.PrivateAccessor;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.PrintWriter;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public final class MultiPackageUpdateServletTest {

    private final MultiPackageUpdateServlet model = new MultiPackageUpdateServlet();

    @Mock
    private MultiPackageUpdateServletConfig config;

    @Mock
    private SlingHttpServletRequest request;

    @Mock
    private SlingHttpServletResponse response;

    @Mock
    private PrintWriter writer;

    @Mock
    private MultiPackageUpdate updater;

    @Before
    public void setUp() throws IOException, NoSuchFieldException {
        PrivateAccessor.setField(model, "updater", updater);

        when(response.getWriter())
                .thenReturn(writer);
    }

    @Test
    public void activate() {
        model.activate(config);
    }

    private void doPost(final String cmd) throws IOException {
        when(request.getParameter(MultiPackageUpdateServlet.CMD))
                .thenReturn(cmd);
        model.doPost(request, response);
    }

    @Test
    public void doPost_noCmd() throws IOException {
        doPost(null);
        verify(writer, never()).write(anyString());
    }

    private void verifyWriteHappened() {
        verify(writer).write(anyString());
    }

    @Test
    public void doPost_start() throws IOException {
        doPost(MultiPackageUpdateServlet.START);
        verify(updater).start(any(PackagesListEndpoint.class), anyString());
        verifyWriteHappened();
    }

    @Test
    public void doPost_stop() throws IOException {
        doPost(MultiPackageUpdateServlet.STOP);
        verify(updater).stop();
        verifyWriteHappened();
    }

    @Test
    public void doPost_currentStatus() throws IOException {
        doPost(MultiPackageUpdateServlet.CURRENT_STATUS);
        verify(updater).getCurrentStatus();
        verifyWriteHappened();
    }

    @Test
    public void doPost_lastLog() throws IOException {
        doPost(MultiPackageUpdateServlet.LAST_LOG);
        verify(updater).getLastLogText();
        verifyWriteHappened();
    }
}