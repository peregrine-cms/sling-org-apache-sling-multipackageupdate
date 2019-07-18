package com.headwire.sling.multipackageupdate;

import javax.jcr.Session;

public interface MultiPackageUpdatePerformerFactory {

    ProcessPerformer createPerformer(PackagesListEndpoint endpoint, Session session, ProcessPerformerListener listener);

}
