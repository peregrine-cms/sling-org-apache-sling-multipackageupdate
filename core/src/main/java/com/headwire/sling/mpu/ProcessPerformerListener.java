package com.headwire.sling.mpu;

public interface ProcessPerformerListener {

    boolean setProcessPerformer(ProcessPerformer performer);

    void notifyProcessFinished(String logText);

}
