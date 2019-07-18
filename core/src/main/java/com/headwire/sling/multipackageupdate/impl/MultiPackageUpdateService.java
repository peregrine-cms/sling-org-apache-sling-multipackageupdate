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
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.HashMap;
import java.util.Map;

@Component(service = { MultiPackageUpdate.class, MultiPackageUpdateService.class })
public final class MultiPackageUpdateService implements MultiPackageUpdate {

    private static final String NO_UPDATE_NO_UPDATE_JOB_RUNNING_CURRENTLY = "No update job running currently";

    private final Object lock = new Object();

    @Reference
    private JobManager jobManager;

    private Job currentJob;

    private MultiPackageUpdateRunner currentRunner;

    private String lastLogText;

    @Override
    public MultiPackageUpdateResponse start(final PackagesListEndpoint endpoint, final String subServiceName) {
        synchronized(lock) {
            if (currentJob == null) {
                return addJob(endpoint, subServiceName);
            }

            if (currentRunner == null) {
                return new MultiPackageUpdateResponse("Update job already scheduled");
            }

            final MultiPackageUpdateResponse response = new MultiPackageUpdateResponse("Update job already in progress");
            response.setLog(currentRunner.getLogText());
            return response;
        }
    }

    private MultiPackageUpdateResponse addJob(final PackagesListEndpoint endpoint, final String subServiceName) {
        final Map<String, Object> params = new HashMap<>();
        params.put(MultiPackageUpdateJobConsumer.ENDPOINT, endpoint);
        params.put(MultiPackageUpdateJobConsumer.SUB_SERVICE_NAME, subServiceName);
        currentJob = jobManager.addJob(MultiPackageUpdateJobConsumer.TOPIC, params);
        return new MultiPackageUpdateResponse("Update job added just now");
    }

    @Override
    public  MultiPackageUpdateResponse stop() {
        synchronized (lock) {
            if (currentJob == null) {
                return new MultiPackageUpdateResponse("No update job scheduled");
            }

            if (currentRunner == null) {
                jobManager.stopJobById(currentJob.getId());
                currentJob = null;
                return new MultiPackageUpdateResponse("Update job stopped");
            }

            currentRunner.terminate();
            final MultiPackageUpdateResponse response = new MultiPackageUpdateResponse("Update job (in progress) marked for earlier termination");
            response.setLog(currentRunner.getLogText());
            return response;
        }
    }

    @Override
    public  MultiPackageUpdateResponse getCurrentStatus() {
        synchronized (lock) {
            if (currentRunner == null) {
                return new MultiPackageUpdateResponse(NO_UPDATE_NO_UPDATE_JOB_RUNNING_CURRENTLY);
            } else {
                final MultiPackageUpdateResponse response = new MultiPackageUpdateResponse("Update process in progress");
                response.setLog(currentRunner.getLogText());
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

    public boolean setCurrentRunner(final MultiPackageUpdateRunner runner) {
        synchronized (lock) {
            if (currentJob == null) {
                return false;
            }

            currentRunner = runner;
        }

        return true;
    }

    public void notifyUpdateProcessFinished(final String logText) {
        synchronized(lock) {
            currentJob = null;
            currentRunner = null;
            lastLogText = logText;
        }
    }
}