package com.headwire.sling.multipackageupdate;

public interface ProcessPerformer {

    void run();

    void terminate();

    String getLogText();

}
