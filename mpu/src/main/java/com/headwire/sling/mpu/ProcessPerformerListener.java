package com.headwire.sling.mpu;

public interface ProcessPerformerListener {

    boolean setProcessPerformer(String endpointName, ProcessPerformer performer);

    void notifyProcessUpdate(PackagesListEndpoint endpoint);

}
