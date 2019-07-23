package com.headwire.sling.multipackageupdate.impl;

import com.headwire.sling.multipackageupdate.MultiPackageUpdateResponse;
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

    private final MultiPackageUpdateServlet model = new MultiPackageUpdateServlet() {

        @Override
        protected MultiPackageUpdateResponse execute() {
            return new MultiPackageUpdateResponse(null);
        }

    };

    @Mock
    private SlingHttpServletRequest request;

    @Mock
    private SlingHttpServletResponse response;

    @Mock
    private PrintWriter writer;

    @Before
    public void setUp() throws IOException {
        when(response.getWriter())
                .thenReturn(writer);
    }

    @Test
    public void doPost() throws IOException {
        model.doPost(request, response);
        verify(writer).write(anyString());
    }
}