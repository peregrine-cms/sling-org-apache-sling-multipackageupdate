package com.headwire.sling.mpu.impl;

import com.headwire.sling.mpu.MultiPackageUpdate.ServiceStatus;
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

import java.util.List;
import java.util.Map;

import static com.headwire.sling.mpu.PackagesListEndpointTest.PACKAGES_TXT;
import static com.headwire.sling.mpu.PackagesListEndpointTest.SERVER_URL;
import static com.headwire.sling.mpu.impl.MultiPackageUpdateService.LAST_LOG;
import static com.headwire.sling.mpu.impl.MultiPackageUpdateService.STATUS;
import static com.headwire.sling.mpu.impl.MultiPackageUpdateService.STATUS_FOR_ENDPOINT;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public final class MultiPackageUpdateServiceTest {

    private final MultiPackageUpdateService model = new MultiPackageUpdateService();
    private final PackagesListEndpoint endpoint = new PackagesListEndpoint(TEST_CONFIG_NAME, SERVER_URL, PACKAGES_TXT, 1);
    private final String subServiceName = "my-sub-service";
    public static final String TEST_CONFIG_NAME = "test";
    public static final String TEST_JOB_ID = "test-job";

    @Mock
    private JobManager jobManager;

    @Mock
    private Job job;

    @Mock
    private ProcessPerformer performer;

    @Mock
    private MultiPackageUpdateServiceConfig config;

    @Before
    public void setUp() throws NoSuchFieldException {
        when(config.getName()).thenReturn(TEST_CONFIG_NAME);
        PrivateAccessor.setField(model, "jobManager", jobManager);
        model.update(config, null);

        when(jobManager.addJob(anyString(), any(Map.class)))
                .thenReturn(job);
        when(job.getId()).thenReturn(TEST_JOB_ID);
    }

    @Test
    public void start() {
        MultiPackageUpdateResponseImpl response = model.start(TEST_CONFIG_NAME);
//        assertNotNull(response.getLog());
        assertTrue(response.getDetails().isEmpty());

        response = model.start(TEST_CONFIG_NAME);
//        assertNotNull(response.getLog());
        assertTrue(response.getDetails().isEmpty());

        model.setProcessPerformer(TEST_CONFIG_NAME, performer);
        response = model.start(TEST_CONFIG_NAME);
//        assertNotNull(response.getLog());
        assertTrue(response.getDetails().isEmpty());
    }

    @Test
    public void stop() {
        MultiPackageUpdateResponseImpl response = model.stop(TEST_CONFIG_NAME);
//        assertNotNull(response.getLog());
        assertTrue(response.getDetails().isEmpty());

        model.start(TEST_CONFIG_NAME);
        response = model.stop(TEST_CONFIG_NAME);
//        assertNotNull(response.getLog());
        assertTrue(response.getDetails().isEmpty());

        model.start(TEST_CONFIG_NAME);
        model.setProcessPerformer(TEST_CONFIG_NAME, performer);
        response = model.stop(TEST_CONFIG_NAME);
//        assertNotNull(response.getLog());
        assertTrue(response.getDetails().isEmpty());
    }

    @Test
    public void getCurrentStatus_noPerformer() {
        List<String> logs = model.getStatus(TEST_CONFIG_NAME).getDetails();
//        assertNotNull(log);
        assertFalse(logs.isEmpty());
        assertEquals("Wrong Status", String.format(STATUS_FOR_ENDPOINT, TEST_CONFIG_NAME, ServiceStatus.configured), logs.get(0));
    }

    @Test
    public void getCurrentStatus() {
        model.start(endpoint, subServiceName);
        model.setProcessPerformer(TEST_CONFIG_NAME, performer);
        assertFalse(model.getStatus(TEST_CONFIG_NAME).getDetails().isEmpty());
//        assertNotNull(model.getAction(TEST_CONFIG_NAME).getLog());
    }

    @Test
    public void testNoExecutionResponse() {
        MultiPackageUpdateResponse response = model.getLogs(TEST_CONFIG_NAME);
        List<String> logs = response.getDetails();
//        String log = response.getLog();
        String status = response.getAction();
//        assertNotNull("Log must be provided", log);
        assertTrue("Log Statement should be empty", logs.isEmpty());
        assertNotNull("Log Status must be provided", status);
        assertEquals("Wrong Log Status", LAST_LOG + TEST_CONFIG_NAME, status);
        response = model.getStatus(TEST_CONFIG_NAME);
        logs = response.getDetails();
        status = response.getAction();
//        assertNotNull("Status Log must be provided", log);
        assertEquals("Wrong Status", String.format(STATUS_FOR_ENDPOINT, TEST_CONFIG_NAME, ServiceStatus.configured), logs.get(0));
        assertNotNull("Status Status must be provided", status);
        assertEquals("Wrong Status Status", STATUS + TEST_CONFIG_NAME, status);
    }

    @Test
    public void setProcessPerformer() {
        assertFalse(model.setProcessPerformer(TEST_CONFIG_NAME, performer));
    }

//    @Test
//    public void notifyProcessFinished() {
//        model.notifyProcessFinished("Example Log");
//        assertNotNull(model.getLastLogText().getLog());
//    }
}