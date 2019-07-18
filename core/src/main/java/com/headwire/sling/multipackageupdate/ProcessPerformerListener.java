package com.headwire.sling.multipackageupdate;

public interface ProcessPerformerListener {

    boolean setProcessPerformer(ProcessPerformer performer);

    void notifyProcessFinished(String logText);

}
