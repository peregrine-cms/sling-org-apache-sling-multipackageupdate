package com.headwire.sling.multipackageupdate.impl;

import com.headwire.sling.multipackageupdate.PackagesListEndpoint;
import com.headwire.sling.multipackageupdate.PackagesListEndpointTest;
import com.headwire.sling.multipackageupdate.ProcessPerformerListener;
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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public final class MultiPackageUpdatePerformerTest {

    private static final String SERVER_URL = MultiPackageUpdatePerformerTest.class.getResource("/").toString();

    private final PackagesListEndpoint endpoint = new PackagesListEndpoint(SERVER_URL, PackagesListEndpointTest.PACKAGES_TXT);
    private final JcrPackageManager packageManager = mock(JcrPackageManager.class);
    private final ProcessPerformerListener listener = mock(ProcessPerformerListener.class);
    private final MultiPackageUpdatePerformer model = new MultiPackageUpdatePerformer(endpoint, packageManager, listener, 1);

    @Mock
    private JcrPackage pack;

    @Before
    public void setUp() throws IOException, RepositoryException {
        when(packageManager.upload(any(InputStream.class), anyBoolean())).thenReturn(pack);
    }

    @Test
    public void run_noPackages() throws IOException, RepositoryException {
        final PackagesListEndpoint endpoint = new PackagesListEndpoint(SERVER_URL, null);
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
    	assertNotNull(model.getLogText());
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
                    return pack;
                });
        model.run();
        verify(pack, never()).install(any(ImportOptions.class));
    }
}