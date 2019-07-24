package com.headwire.sling.mpu.impl;

import com.headwire.sling.mpu.MultiPackageUpdateResponse;
import com.headwire.sling.mpu.PackagesListEndpoint;
import com.headwire.sling.mpu.ProcessPerformer;
import junitx.util.PrivateAccessor;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static com.headwire.sling.mpu.PackagesListEndpointTest.PACKAGES_TXT;
import static com.headwire.sling.mpu.PackagesListEndpointTest.SERVER_URL;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public final class MultiPackageUpdateServiceTest {

    private final MultiPackageUpdateService model = new MultiPackageUpdateService();
    private final PackagesListEndpoint endpoint = new PackagesListEndpoint(SERVER_URL, PACKAGES_TXT);
    private final String subServiceName = "my-sub-service";

    @Mock
    private JobManager jobManager;

    @Mock
    private Job job;

    @Mock
    private ProcessPerformer performer;

    @Before
    public void setUp() throws NoSuchFieldException {
        PrivateAccessor.setField(model, "jobManager", jobManager);
        when(jobManager.addJob(anyString(), any(Map.class)))
                .thenReturn(job);
        when(performer.getLogText())
                .thenReturn("Logged Info");
    }

    @Test
    public void start() {
        MultiPackageUpdateResponse response = model.start(endpoint, subServiceName, 1);
        assertNull(response.getLog());

        response = model.start(endpoint, subServiceName, 1);
        assertNull(response.getLog());

        model.setProcessPerformer(performer);
        response = model.start(endpoint, subServiceName, 1);
        assertNotNull(response.getLog());
    }

    @Test
    public void stop() {
        MultiPackageUpdateResponse response = model.stop();
        assertNull(response.getLog());

        model.start(endpoint, subServiceName, 1);
        response = model.stop();
        assertNull(response.getLog());

        model.start(endpoint, subServiceName, 1);
        model.setProcessPerformer(performer);
        response = model.stop();
        assertNotNull(response.getLog());
    }

    @Test
    public void getCurrentStatus_noPerformer() {
        assertNull(model.getCurrentStatus().getLog());
    }

    @Test
    public void getCurrentStatus() {
        model.start(endpoint, subServiceName, 1);
        model.setProcessPerformer(performer);
        assertNotNull(model.getCurrentStatus().getLog());
    }

    @Test
    public void getLastLogText() {
        assertNull(model.getLastLogText().getLog());
    }

    @Test
    public void setProcessPerformer() {
        assertFalse(model.setProcessPerformer(performer));
    }

    @Test
    public void notifyProcessFinished() {
        model.notifyProcessFinished("Example Log");
        assertNotNull(model.getLastLogText().getLog());
    }
}