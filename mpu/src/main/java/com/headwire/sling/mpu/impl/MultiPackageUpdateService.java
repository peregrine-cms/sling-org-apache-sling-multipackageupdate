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
import com.headwire.sling.mpu.MultiPackageUpdateResponse;
import com.headwire.sling.mpu.MultiPackageUpdateResponse.Code;
import com.headwire.sling.mpu.PackagesListEndpoint;
import com.headwire.sling.mpu.ProcessPerformer;
import com.headwire.sling.mpu.ProcessPerformerListener;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.headwire.sling.mpu.MPUConstants.PROJECT_NAME;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component(service = { MultiPackageUpdate.class, ProcessPerformerListener.class })
public final class MultiPackageUpdateService implements MultiPackageUpdate, ProcessPerformerListener {

    private static final String JOB_SCHEDULED = "Update job scheduled just now";
    private static final String JOB_SCHEDULED_BEFORE = "Update job already scheduled before";
    private static final String JOB_IN_PROGRESS = "Update job already in progress";
    private static final String UPDATE_JOB_STOPPED = "Update job stopped before running";
    private static final String MARKED_FOR_EARLIER_TERMINATION = "Update job (currently in progress) marked for earlier termination";
    protected static final String NO_UPDATE_JOB_RUNNING = "No update job running currently";
    protected static final String LOG_UNAVAILABLE = "No previous log available";
    protected static final String STATUS_UNAVAILABLE = "No Endpoint available for Status";
    protected static final String ENDPOINT_UNAVAILABLE = "Endpoint not found : ";
    protected static final String LAST_LOG = "Last log: ";
    protected static final String LIST = "Endpoint List: ";
    protected static final String STATUS = "Status: ";
    protected static final String STATUS_FOR_ENDPOINT = "Status for Endpoint: '%s': '%s'";

    private static final String SUB_SERVICE_NAME = PROJECT_NAME;

    private final Object lock = new Object();

    protected Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Reference
    private JobManager jobManager;

    private Map<String, PackagesListEndpoint> endpointMap = new HashMap<>();

    public void update(final MultiPackageUpdateServiceConfig config, String oldName) {
        if(isBlank(oldName)) {
            logger.info("Add new configuration, name: '{}', url: '{}', deactivate: '{}'", config.getName(), config.getServerUrl());
            endpointMap.put(config.getName(), new PackagesListEndpoint(config.getName(), config.getServerUrl(), config.getFilename(), config.getMaxRetries()));
        } else if(config == null) {
            logger.info("Remove existing configuration, name: '{}'", oldName);
            endpointMap.remove(oldName);
        } else {
            String name = config.getName();
            if(!oldName.equals(name)) {
                logger.info("Remove existing configuration before update, name: '{}'", oldName);
                endpointMap.remove(oldName);
            }
            logger.info("Update existing configuration, name: '{}', url: '{}', deactivate: '{}'", name, config.getServerUrl());
            endpointMap.put(name, new PackagesListEndpoint(config.getName(), config.getServerUrl(), config.getFilename(), config.getMaxRetries()));
        }
    }

    @Override
    public MultiPackageUpdateResponseImpl start(final PackagesListEndpoint endpoint, final String subServiceName) {
        MultiPackageUpdateResponseImpl answer;
        synchronized(lock) {
            switch(endpoint.getStatus()) {
                case started:
                    answer = createResponse(Code.WAITING, JOB_SCHEDULED_BEFORE);
                    break;
                case running:
                    answer = createResponse(Code.WAITING, JOB_IN_PROGRESS);
                    break;
                default:
                    Job job = createJob(endpoint, subServiceName, endpoint.getMaxRetries());
                    endpoint.setJobId(job.getId());
                    answer = createResponse(Code.SCHEDULED, JOB_SCHEDULED);
            }
        }
        return answer;
    }

    @Override
    public MultiPackageUpdateResponseImpl start(String name) {
        MultiPackageUpdateResponseImpl answer;
        PackagesListEndpoint endpoint = endpointMap.get(name);
        if(endpoint != null) {
            // Clear Logs on a new start
            endpoint.setLogs(null);
            endpoint.log("Start Endpoint: " + name);
            answer = start(endpoint, SUB_SERVICE_NAME);
        } else {
            answer = new MultiPackageUpdateResponseImpl();
            answer.setCode(Code.UNAVAILABLE);
            answer.setAction("Start Failed, Config with name: '" + name + "' not found");
        }
        return answer;
    }

    private Job createJob(final PackagesListEndpoint endpoint, final String subServiceName, final int maxRetriesCount) {
        final Map<String, Object> params = new HashMap<>();
        params.put(MultiPackageUpdateJobConsumer.ENDPOINT, endpoint);
        params.put(MultiPackageUpdateJobConsumer.SUB_SERVICE_NAME, subServiceName);
        params.put(MultiPackageUpdateJobConsumer.MAX_RETRIES_COUNT, maxRetriesCount);
        logger.info("Add Job for Endpoint: '{}'", endpoint.getServerUrl());
        endpoint.setStatus(ServiceStatus.started);
        return jobManager.addJob(MultiPackageUpdateJobConsumer.TOPIC, params);
    }

    private MultiPackageUpdateResponseImpl createResponse(final Code code, final String status) {
        final MultiPackageUpdateResponseImpl response = new MultiPackageUpdateResponseImpl();
        response.setCode(code);
        response.setAction(status);
        return response;
    }

    @Override
    public MultiPackageUpdateResponse getList() {
        MultiPackageUpdateResponseImpl answer;
        List<String> names = new ArrayList<>(endpointMap.keySet());
        answer = createResponse(Code.AVAILABLE, LIST)
            .setDetails(names);
        return answer;
    }

    @Override
    public MultiPackageUpdateResponse getLogs(String name) {
        MultiPackageUpdateResponseImpl answer;
        if(endpointMap.isEmpty()) {
            answer = createResponse(Code.UNAVAILABLE, LOG_UNAVAILABLE);
        } else {
            PackagesListEndpoint endpoint = endpointMap.get(name);
            if(endpoint == null) {
                answer = createResponse(Code.UNAVAILABLE, ENDPOINT_UNAVAILABLE + name);
            } else {
                logger.info("Get Logs From Endpoint: '{}'", endpoint);
                answer = createResponse(Code.AVAILABLE, LAST_LOG + name)
                    .setDetails(endpoint.getLogs());
            }
        }
        return answer;
    }

    @Override
    public MultiPackageUpdateResponse getStatus(String name) {
        MultiPackageUpdateResponseImpl answer;
        if(endpointMap.isEmpty()) {
            answer = createResponse(Code.UNAVAILABLE, STATUS_UNAVAILABLE);
        } else {
            PackagesListEndpoint endpoint = endpointMap.get(name);
            if(endpoint == null) {
                answer = createResponse(Code.UNAVAILABLE, ENDPOINT_UNAVAILABLE + name);
            } else {
                answer = createResponse(Code.AVAILABLE, STATUS + name)
                    .setDetails(Arrays.asList(new String[] { String.format(STATUS_FOR_ENDPOINT, name, endpoint.getStatus()) }));
            }
        }
        return answer;
    }

    @Override
    public MultiPackageUpdateResponseImpl stop(String name) {
        MultiPackageUpdateResponseImpl answer;
        PackagesListEndpoint endpoint = endpointMap.get(name);
        if(endpoint != null) {
            endpoint.log("Terminate Endpoint: " + name);
            synchronized (lock) {
                ProcessPerformer performer = endpoint.getPerformer();
                String jobId = endpoint.getJobId();
                if(!jobId.isEmpty()) {
                    jobManager.stopJobById(endpoint.getJobId());
                }
                if(performer != null) {
                    performer.terminate();
                    answer = createResponse(Code.AWAITING_TERMINATION, MARKED_FOR_EARLIER_TERMINATION);
                } else {
                    answer = createResponse(Code.TERMINATED, UPDATE_JOB_STOPPED);
                }
            }
        } else {
            answer = new MultiPackageUpdateResponseImpl();
            answer.setCode(Code.UNAVAILABLE);
            answer.setAction("Stop Failed, Config with name: '" + name + "' not found");
        }
        return answer;
    }

    public boolean setProcessPerformer(final String endpointName, final ProcessPerformer runner) {
        boolean answer = false;
        synchronized (lock) {
            PackagesListEndpoint endpoint = endpointMap.get(endpointName);
            if(endpoint != null) {
                String jobId = endpoint.getJobId();
                if(isNotBlank(jobId)) {
                    endpoint.setPerformer(runner);
                    answer = true;
                } else {
                    answer = false;
                }
            }
        }

        return answer;
    }

    @Override
    public void notifyProcessUpdate(final PackagesListEndpoint endpoint) {
        synchronized(lock) {
            PackagesListEndpoint myEndpoint = endpointMap.get(endpoint.getName());
            if(myEndpoint != null) {
                myEndpoint.setStatus(endpoint.getStatus());
                myEndpoint.setLogs(endpoint.getLogs());
            }
        }
    }
}