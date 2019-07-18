package com.headwire.sling.multipackageupdate.impl;

import com.headwire.sling.multipackageupdate.*;
import org.apache.jackrabbit.vault.packaging.JcrPackageManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Session;

@Component(service = { MultiPackageUpdatePerformerFactory.class })
public final class MultiPackageUpdatePerformerFactoryImpl implements MultiPackageUpdatePerformerFactory {

    @Reference
    private JcrPackageManagerFactory jcrPackageManagerFactory;

    @Override
    public ProcessPerformer createPerformer(PackagesListEndpoint endpoint, Session session, ProcessPerformerListener listener) {
        final JcrPackageManager packageManager = jcrPackageManagerFactory.createJcrPackageManager(session);
        return new MultiPackageUpdatePerformer(endpoint, packageManager, listener);
    }

}
