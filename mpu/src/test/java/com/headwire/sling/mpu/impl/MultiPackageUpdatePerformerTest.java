package com.headwire.sling.mpu.impl;

import com.headwire.sling.mpu.PackagesListEndpoint;
import com.headwire.sling.mpu.PackagesListEndpointTest;
import com.headwire.sling.mpu.ProcessPerformerListener;
import org.apache.jackrabbit.vault.fs.io.ImportOptions;
import org.apache.jackrabbit.vault.packaging.JcrPackage;
import org.apache.jackrabbit.vault.packaging.JcrPackageManager;
import org.apache.jackrabbit.vault.packaging.PackageException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;

import static com.headwire.sling.mpu.impl.MultiPackageUpdateServiceTest.TEST_CONFIG_NAME;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public final class MultiPackageUpdatePerformerTest {

    private static final String SERVER_URL = MultiPackageUpdatePerformerTest.class.getResource("/").toString();

    private final PackagesListEndpoint endpoint = new PackagesListEndpoint(TEST_CONFIG_NAME, SERVER_URL, PackagesListEndpointTest.PACKAGES_TXT, 1);
    private final JcrPackageManager packageManager = mock(JcrPackageManager.class);
    private final ProcessPerformerListener listener = mock(ProcessPerformerListener.class);
    private final MultiPackageUpdatePerformer model = new MultiPackageUpdatePerformer(endpoint, packageManager, listener, 3);

    @Mock
    private JcrPackage pack;

    @Before
    public void setUp() throws IOException, RepositoryException {
        when(packageManager.upload(any(InputStream.class), anyBoolean())).thenReturn(pack);
    }

    @Test
    public void run_noPackages() throws IOException, RepositoryException {
        final PackagesListEndpoint endpoint = new PackagesListEndpoint(TEST_CONFIG_NAME, SERVER_URL, null, 1);
        final MultiPackageUpdatePerformer model = new MultiPackageUpdatePerformer(endpoint, packageManager, listener, 1);
    	model.run();
    	verify(packageManager, never()).upload(any(InputStream.class), anyBoolean());
    }

    @Test
    public void run() throws IOException, RepositoryException {
        model.run();
        verify(packageManager, times(3)).upload(any(InputStream.class), anyBoolean());
    }

    @Test
    public void onMessage() {
    	model.onMessage(null, null, null);
        getLogText();
    }

    @Test
    public void onError() {
    	model.onError(null, null, new Exception());
        getLogText();
    }

    @Test
    public void getLogText() {
    	assertNotNull(endpoint.getLogs());
    }

    @Test
    public void terminate_firstOccurrence() throws IOException, RepositoryException {
        model.terminate();
        model.run();
        verify(packageManager, never()).upload(any(InputStream.class), anyBoolean());
    }

    @Test
    public void terminate_secondOccurrence() throws IOException, RepositoryException, PackageException {
        when(packageManager.upload(any(InputStream.class), anyBoolean()))
                .thenAnswer(invocation -> {
                    model.terminate();
                    throw new RepositoryException();
                });
        model.run();
        verify(pack, never()).install(any(ImportOptions.class));
    }

    @Test
    public void terminate_thirdOccurrence() throws IOException, RepositoryException, PackageException {
        when(packageManager.upload(any(InputStream.class), anyBoolean()))
                .thenAnswer(invocation -> {
                    model.terminate();
                    return pack;
                });
        model.run();
        verify(pack, never()).install(any(ImportOptions.class));
    }

    @Test
    public void failAllRetries() throws IOException, RepositoryException, PackageException {
        when(packageManager.upload(any(InputStream.class), anyBoolean()))
                .thenThrow(new RepositoryException());
        model.run();
        verify(pack, never()).install(any(ImportOptions.class));
    }

    @Test
    public void cover0Retries() throws IOException, RepositoryException, PackageException {
    	final MultiPackageUpdatePerformer model = new MultiPackageUpdatePerformer(endpoint, packageManager, listener, 0);
        model.run();
        verify(pack, never()).install(any(ImportOptions.class));
    }

}