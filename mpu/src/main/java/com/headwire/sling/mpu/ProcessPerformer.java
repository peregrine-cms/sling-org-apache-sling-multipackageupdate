package com.headwire.sling.mpu;

public interface ProcessPerformer {

    void run();

    void terminate();

    boolean isTerminated();
}
