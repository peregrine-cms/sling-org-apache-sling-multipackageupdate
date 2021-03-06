package com.headwire.sling.mpu.impl;

import com.headwire.sling.mpu.MultiPackageUpdatePerformerFactory;
import com.headwire.sling.mpu.PackagesListEndpoint;
import com.headwire.sling.mpu.ProcessPerformer;
import com.headwire.sling.mpu.ProcessPerformerListener;
import junitx.util.PrivateAccessor;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer.JobResult;
import org.apache.sling.jcr.api.SlingRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static com.headwire.sling.mpu.PackagesListEndpointTest.PACKAGES_TXT;
import static com.headwire.sling.mpu.PackagesListEndpointTest.SERVER_URL;
import static com.headwire.sling.mpu.impl.MultiPackageUpdateServiceTest.TEST_CONFIG_NAME;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public final class MultiPackageUpdateJobConsumerTest {

    private final MultiPackageUpdateJobConsumer model = new MultiPackageUpdateJobConsumer();

    private final PackagesListEndpoint endpoint = new PackagesListEndpoint(TEST_CONFIG_NAME, SERVER_URL, PACKAGES_TXT, 1);

    private final String subServiceName = "my-sub-service";

    @Mock
    private SlingRepository repository;

    @Mock
    private MultiPackageUpdatePerformerFactory multiPackageUpdatePerformerFactory;

    @Mock
    private Job job;

    @Mock
    private Session session;

    @Mock
    private ProcessPerformer performer;

    @Mock
    private ProcessPerformerListener processPerformerListener;

    @Before
    public void setUp() throws NoSuchFieldException, RepositoryException {
        PrivateAccessor.setField(model, "repository", repository);
        PrivateAccessor.setField(model, "multiPackageUpdatePerformerFactory", multiPackageUpdatePerformerFactory);
        PrivateAccessor.setField(model, "processPerformerListener", processPerformerListener);

        when(job.getProperty(MultiPackageUpdateJobConsumer.ENDPOINT, PackagesListEndpoint.class))
                .thenReturn(endpoint);
        when(job.getProperty(MultiPackageUpdateJobConsumer.SUB_SERVICE_NAME, String.class))
                .thenReturn(subServiceName);
        when(job.getProperty(MultiPackageUpdateJobConsumer.MAX_RETRIES_COUNT, 1))
                .thenReturn(1);

        when(repository.loginService(subServiceName, null))
                .thenReturn(session);

        when(multiPackageUpdatePerformerFactory.createPerformer(eq(endpoint), eq(session), eq(processPerformerListener), anyInt()))
                .thenReturn(performer);
    }

    @Test
    public void process_run() {
        when(processPerformerListener.setProcessPerformer(TEST_CONFIG_NAME, performer))
                .thenReturn(true);
        assertEquals(JobResult.OK, model.process(job));
        verify(performer).run();
    }

    @Test
    public void process_doNotRun() {
        when(processPerformerListener.setProcessPerformer(TEST_CONFIG_NAME, performer))
                .thenReturn(false);
        assertEquals(JobResult.OK, model.process(job));
        verify(performer, never()).run();
    }

    @Test
    public void process_catchRepositoryException() throws RepositoryException {
        when(repository.loginService(subServiceName, null))
                .thenThrow(new RepositoryException());
        assertEquals(JobResult.CANCEL, model.process(job));
//        verify(processPerformerListener).notifyProcessFinished(anyString());
    }
}