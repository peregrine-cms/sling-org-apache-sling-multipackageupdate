package com.headwire.sling.multipackageupdate.impl;

import com.headwire.sling.multipackageupdate.*;
import org.osgi.service.component.annotations.Component;

import javax.jcr.Session;

@Component(service = { MultiPackageUpdatePerformerFactory.class })
public final class MultiPackageUpdatePerformerFactoryImpl implements MultiPackageUpdatePerformerFactory {

    @Override
    public ProcessPerformer createPerformer(PackagesListEndpoint endpoint, Session session, ProcessPerformerListener listener) {
        return new MultiPackageUpdateRunner(endpoint, session, listener);
    }

}
