package com.headwire.sling.mpu;

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

import com.headwire.sling.mpu.MultiPackageUpdate.ServiceStatus;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class PackagesListEndpoint implements Serializable {

    private final String name;
    private final String serverUrl;
    private final String packagesFileName;
    private final int maxRetries;
    private ServiceStatus status = ServiceStatus.empty;
    private List<String> logs = new ArrayList<>();
    private String jobId = "";
    private ProcessPerformer performer;

    public PackagesListEndpoint(final String name, final String serverUrl, final String packagesFileName, int maxRetries) {
        this.name = name;
        this.serverUrl = serverUrl;
        this.packagesFileName = packagesFileName;
        this.maxRetries = maxRetries <= 0 ? 1 : maxRetries;
        status = ServiceStatus.configured;
    }

    private static boolean isUrlValid(final String url) {
        final String lowercaseUrl = StringUtils.lowerCase(url);
        return StringUtils.startsWithAny(lowercaseUrl, "http://", "https://");
    }

    public String getName() {
        return name;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getFileUrl(final String name) {
        if (isUrlValid(name)) {
            return name;
        }

        return getServerUrl() + "/" + name;
    }

    public String getPackagesListUrl() {
        return getFileUrl(packagesFileName);
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public ServiceStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceStatus status) {
        this.status = status;
    }

    public List<String> getLogs() {
        return logs;
    }

    public void setLogs(List<String> logs) {
        this.logs = logs == null ? new ArrayList<>() : logs;
    }

    public void log(String message) {
        logs.add(message);
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public ProcessPerformer getPerformer() {
        return performer;
    }

    public void setPerformer(ProcessPerformer performer) {
        this.performer = performer;
    }
}