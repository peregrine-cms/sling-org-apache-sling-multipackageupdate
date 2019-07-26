package com.headwire.sling.mpu.impl;

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

import com.headwire.sling.mpu.MultiPackageUpdate;
import com.headwire.sling.mpu.MultiPackageUpdateResponse.Code;
import com.headwire.sling.mpu.PackagesListEndpoint;
import com.headwire.sling.mpu.ProcessPerformer;
import com.headwire.sling.mpu.ProcessPerformerListener;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

import java.util.HashMap;
import java.util.Map;

import static com.headwire.sling.mpu.MPUConstants.PROJECT_NAME;

@Component(service = { MultiPackageUpdate.class, ProcessPerformerListener.class })
@Designate(ocd = MultiPackageUpdateServiceConfig.class)
public final class MultiPackageUpdateService implements MultiPackageUpdate, ProcessPerformerListener {

    private static final String JOB_SCHEDULED = "Update job scheduled just now";
    private static final String JOB_SCHEDULED_BEFORE = "Update job already scheduled before";
    private static final String JOB_IN_PROGRESS = "Update job already in progress";
    private static final String NO_UPDATE_JOB_SCHEDULED = "No update job scheduled currently";
    private static final String UPDATE_JOB_STOPPED = "Update job stopped before running";
    private static final String MARKED_FOR_EARLIER_TERMINATION = "Update job (currently in progress) marked for earlier termination";
    private static final String NO_UPDATE_JOB_RUNNING = "No update job running currently";
    private static final String LOG_UNAVAILABLE = "No previous log available";
    private static final String LAST_LOG = "Last log";

    private static final String SUB_SERVICE_NAME = PROJECT_NAME;

    private final Object lock = new Object();

    @Reference
    private JobManager jobManager;

    private MultiPackageUpdateServiceConfig config;

    private PackagesListEndpoint endpoint;

    private Job currentJob;

    private ProcessPerformer currentPerformer;

    private String lastLogText;

    @Activate
    public void activate(final MultiPackageUpdateServiceConfig config) {
        this.config = config;
        endpoint = new PackagesListEndpoint(config.serverUrl(), config.filename());
    }

    @Override
    public MultiPackageUpdateResponseImpl start(final PackagesListEndpoint endpoint, final String subServiceName, final int retryCounter) {
        synchronized(lock) {
            if (currentJob == null) {
                currentJob = createJob(endpoint, subServiceName, retryCounter);
                return createResponse(Code.SCHEDULED, JOB_SCHEDULED);
            }

            if (currentPerformer == null) {
                return createResponse(Code.WAITING, JOB_SCHEDULED_BEFORE);
            }

            return createResponseWithCurrentLogText(Code.IN_PROGRESS, JOB_IN_PROGRESS);
        }
    }

    @Override
    public MultiPackageUpdateResponseImpl start() {
        return start(endpoint, SUB_SERVICE_NAME, config.maxRetriesCount());
    }

    private Job createJob(final PackagesListEndpoint endpoint, final String subServiceName, final int maxRetriesCount) {
        final Map<String, Object> params = new HashMap<>();
        params.put(MultiPackageUpdateJobConsumer.ENDPOINT, endpoint);
        params.put(MultiPackageUpdateJobConsumer.SUB_SERVICE_NAME, subServiceName);
        params.put(MultiPackageUpdateJobConsumer.MAX_RETRIES_COUNT, maxRetriesCount);
        return jobManager.addJob(MultiPackageUpdateJobConsumer.TOPIC, params);
    }

    private MultiPackageUpdateResponseImpl createResponse(final Code code, final String status) {
        final MultiPackageUpdateResponseImpl response = new MultiPackageUpdateResponseImpl();
        response.setCode(code);
        response.setStatus(status);
        return response;
    }

    private MultiPackageUpdateResponseImpl createResponseWithCurrentLogText(final Code code, final String status) {
        final MultiPackageUpdateResponseImpl response = createResponse(code, status);
        response.setLog(currentPerformer.getLogText());
        return response;
    }

    @Override
    public MultiPackageUpdateResponseImpl stop() {
        synchronized (lock) {
            if (currentJob == null) {
                return createResponse(Code.UNAVAILABLE, NO_UPDATE_JOB_SCHEDULED);
            }

            if (currentPerformer == null) {
                jobManager.stopJobById(currentJob.getId());
                currentJob = null;
                return createResponse(Code.TERMINATED, UPDATE_JOB_STOPPED);
            }

            currentPerformer.terminate();
            return createResponseWithCurrentLogText(Code.AWAITING_TERMINATION, MARKED_FOR_EARLIER_TERMINATION);
        }
    }

    @Override
    public MultiPackageUpdateResponseImpl getCurrentStatus() {
        synchronized (lock) {
            if (currentPerformer == null) {
                return createResponse(Code.UNAVAILABLE, NO_UPDATE_JOB_RUNNING);
            }

            if (currentPerformer.isTerminated()) {
                return createResponseWithCurrentLogText(Code.AWAITING_TERMINATION, MARKED_FOR_EARLIER_TERMINATION);
            }

            return createResponseWithCurrentLogText(Code.IN_PROGRESS, JOB_IN_PROGRESS);
        }
    }

    @Override
    public MultiPackageUpdateResponseImpl getLastLogText() {
        if (StringUtils.isBlank(lastLogText)) {
            return createResponseWithLastLogText(Code.UNAVAILABLE, LOG_UNAVAILABLE);
        }

        return createResponseWithLastLogText(Code.AVAILABLE, LAST_LOG);
    }

    private MultiPackageUpdateResponseImpl createResponseWithLastLogText(final Code code, final String status) {
        final MultiPackageUpdateResponseImpl response = createResponse(code, status);
        response.setLog(lastLogText);
        return response;
    }

    public boolean setProcessPerformer(final ProcessPerformer runner) {
        synchronized (lock) {
            if (currentJob == null) {
                return false;
            }

            currentPerformer = runner;
        }

        return true;
    }

    public void notifyProcessFinished(final String logText) {
        synchronized(lock) {
            currentJob = null;
            currentPerformer = null;
            lastLogText = logText;
        }
    }
}