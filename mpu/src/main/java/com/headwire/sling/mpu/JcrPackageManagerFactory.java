package com.headwire.sling.mpu;

import org.apache.jackrabbit.vault.packaging.JcrPackageManager;

import javax.jcr.Session;

public interface JcrPackageManagerFactory {

    JcrPackageManager createJcrPackageManager(Session session);

}
