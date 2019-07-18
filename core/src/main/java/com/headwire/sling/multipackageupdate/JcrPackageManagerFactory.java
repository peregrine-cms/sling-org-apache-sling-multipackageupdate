package com.headwire.sling.multipackageupdate;

import org.apache.jackrabbit.vault.packaging.JcrPackageManager;

import javax.jcr.Session;

public interface JcrPackageManagerFactory {

    JcrPackageManager createJcrPackageManager(Session session);

}
