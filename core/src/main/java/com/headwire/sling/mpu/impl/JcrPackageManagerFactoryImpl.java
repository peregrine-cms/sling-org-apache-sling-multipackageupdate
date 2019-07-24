package com.headwire.sling.mpu.impl;

import com.headwire.sling.mpu.JcrPackageManagerFactory;
import org.apache.jackrabbit.vault.packaging.JcrPackageManager;
import org.apache.jackrabbit.vault.packaging.PackagingService;
import org.osgi.service.component.annotations.Component;

import javax.jcr.Session;

@Component(service = { JcrPackageManagerFactory.class })
public final class JcrPackageManagerFactoryImpl implements JcrPackageManagerFactory {

    @Override
    public JcrPackageManager createJcrPackageManager(final Session session) {
        return PackagingService.getPackageManager(session);
    }

}
