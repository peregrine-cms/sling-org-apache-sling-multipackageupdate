package com.headwire.sling.mpu;

import javax.jcr.Session;

public interface MultiPackageUpdatePerformerFactory {

    ProcessPerformer createPerformer(PackagesListEndpoint endpoint, Session session, ProcessPerformerListener listener, int maxRetriesCount);

}
