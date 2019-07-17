package com.headwire.sling.multipackageupdate.impl;

/*-
 * #%L
 * Multi Package Update - Core
 * %%
 * Copyright (C) 2017 headwire inc.
 * %%
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * #L%
 */

import com.headwire.sling.multipackageupdate.MultiPackageUpdate;
import com.headwire.sling.multipackageupdate.MultiPackageUpdateResponse;
import com.headwire.sling.multipackageupdate.PackagesListEndpoint;
import com.headwire.sling.multipackageupdate.PackagesUpdatedListener;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

@Component(service = MultiPackageUpdate.class)
@Designate(ocd = MultiPackageUpdateServletConfig.class)
public final class MultiPackageUpdateService implements MultiPackageUpdate, PackagesUpdatedListener {

    private static final String UNABLE_TO_OBTAIN_SESSION = "Unable to obtain session";
    private static final String NO_UPDATE_THREAD_RUNNING_CURRENTLY = "There is no update thread running currently";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Object lock = new Object();

    @Reference
    private SlingRepository repository;

    private MultiPackageUpdateThread currentThread;

    private String lastLogText;

    @Override
    public MultiPackageUpdateResponse start(final PackagesListEndpoint endpoint, final String subServiceName) {
        synchronized(lock) {
            if (currentThread == null) {
                return startThread(endpoint, subServiceName);
            } else {
                final MultiPackageUpdateResponse response = new MultiPackageUpdateResponse("Update process already in progress");
                response.setLog(currentThread.getLogText());
                return response;
            }
        }
    }

    private MultiPackageUpdateResponse startThread(final PackagesListEndpoint endpoint, final String subServiceName) {
        try {
            final Session session = repository.loginService(subServiceName, null);
            currentThread = new MultiPackageUpdateThread(endpoint, this, session);
            currentThread.start();
            return new MultiPackageUpdateResponse("Update process started just now");
        } catch (final RepositoryException e) {
            logger.error(UNABLE_TO_OBTAIN_SESSION, e);
            final MultiPackageUpdateResponse response = new MultiPackageUpdateResponse(UNABLE_TO_OBTAIN_SESSION);
            response.setLog(ExceptionUtils.getStackTrace(e));
            return response;
        }
    }

    @Override
    public  MultiPackageUpdateResponse stop() {
        synchronized (lock) {
            if (currentThread == null) {
                return new MultiPackageUpdateResponse(NO_UPDATE_THREAD_RUNNING_CURRENTLY);
            } else {
                currentThread.terminate();
                final MultiPackageUpdateResponse response = new MultiPackageUpdateResponse("Update thread marked for earlier termination");
                response.setLog(currentThread.getLogText());
                return response;
            }
        }
    }

    @Override
    public  MultiPackageUpdateResponse getCurrentStatus() {
        synchronized (lock) {
            if (currentThread == null) {
                return new MultiPackageUpdateResponse(NO_UPDATE_THREAD_RUNNING_CURRENTLY);
            } else {
                final MultiPackageUpdateResponse response = new MultiPackageUpdateResponse("Update process in progress");
                response.setLog(currentThread.getLogText());
                return response;
            }
        }
    }

    @Override
    public  MultiPackageUpdateResponse getLastLogText() {
        final String status = StringUtils.isBlank(lastLogText) ? "No previous log available" :  "Last log";
        final MultiPackageUpdateResponse response = new MultiPackageUpdateResponse(status);
        response.setLog(lastLogText);
        return response;
    }

    @Override
    public void notifyPackagesUpdated(final String logText) {
        synchronized(lock) {
            lastLogText = logText;
            currentThread = null;
        }
    }
}