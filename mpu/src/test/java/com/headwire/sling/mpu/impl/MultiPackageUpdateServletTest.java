package com.headwire.sling.mpu.impl;

import com.headwire.sling.mpu.MPUUtil;
import com.headwire.sling.mpu.MultiPackageUpdateResponse;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestPathInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.headwire.sling.mpu.MPUUtil.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public final class MultiPackageUpdateServletTest {

    private final MultiPackageUpdateServlet model = new MultiPackageUpdateServlet() {

        @Override
        protected MultiPackageUpdateResponseImpl execute(String name) {
            return new MultiPackageUpdateResponseImpl();
        }

        @Override
        protected int getStatusCode(final MultiPackageUpdateResponse.Code code) {
            return HttpServletResponse.SC_OK;
        }

        @Override
        protected List<TYPE> getSupportedTypes() {
            return JSON_HTML_TYPES;
        }
    };

    @Mock
    private SlingHttpServletRequest request;

    @Mock
    private SlingHttpServletResponse response;

    @Mock
    private RequestPathInfo pathInfo;

    @Mock
    private PrintWriter writer;

    @Before
    public void setUp() throws IOException {
        when(response.getWriter())
                .thenReturn(writer);
        when(request.getRequestPathInfo())
                .thenReturn(pathInfo);
        when(request.getHeaders(ACCEPT))
                .thenReturn(Collections.emptyEnumeration());
    }

    private void doPostAndVerify(final String contentType, final int contentTypeInvocation, final int writeInvocations) throws IOException {
        model.doPost(request, response);
        verify(response, times(contentTypeInvocation)).setContentType(contentType);
        verify(writer, times(writeInvocations)).println(anyString());
    }

    private void doPostAndVerify(final String contentType) throws IOException {
        doPostAndVerify(contentType, 1, 1);
    }

    @Test
    public void htmlExtension() throws IOException {
        when(pathInfo.getExtension())
                .thenReturn(HTML);
        doPostAndVerify(TEXT_HTML, 1, 1);
    }

    @Test
    public void jsonExtension() throws IOException {
        when(pathInfo.getExtension())
                .thenReturn(MPUUtil.JSON);
        doPostAndVerify(APPLICATION_JSON);
    }

    @Test
    public void htmlAcceptHeader() throws IOException {
        when(request.getHeader(ACCEPT))
                .thenReturn(TEXT_HTML);
        doPostAndVerify(TEXT_PLAIN, 1, 1);
    }

    @Test
    public void htmlAcceptHeaders() throws IOException {
        final List<String> headers = new LinkedList<>();
        headers.add(APPLICATION_JSON);
        headers.add(TEXT_HTML);
        headers.add(APPLICATION_JSON);
        when(request.getHeaders(ACCEPT))
                .thenReturn(Collections.enumeration(headers));
        doPostAndVerify(APPLICATION_JSON);
    }

    @Test
    public void nothingDefault() throws IOException {
        doPostAndVerify(TEXT_PLAIN, 1, 1);
    }

}