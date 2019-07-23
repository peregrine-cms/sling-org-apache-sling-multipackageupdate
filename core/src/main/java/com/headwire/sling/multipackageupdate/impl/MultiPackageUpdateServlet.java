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

import com.google.gson.Gson;
import com.headwire.sling.multipackageupdate.MultiPackageUpdate;
import com.headwire.sling.multipackageupdate.MultiPackageUpdateResponse;
import com.headwire.sling.multipackageupdate.PackagesListEndpoint;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.apache.sling.api.servlets.ServletResolverConstants.*;

@Component(service = Servlet.class,
        property = {
                SLING_SERVLET_METHODS + "=POST",
                SLING_SERVLET_RESOURCE_TYPES + "=multipackageupdate/update",
                SLING_SERVLET_SELECTORS + "=json"
        })
@Designate(ocd = MultiPackageUpdateServletConfig.class)
public final class MultiPackageUpdateServlet extends SlingAllMethodsServlet {

    private static final long serialVersionUID = -1704915461516132101L;

    public static final String SUB_SERVICE_NAME = "multipackageupdate";

    public static final String CMD = "cmd";
    public static final String START = "start";
    public static final String STOP = "stop";
    public static final String CURRENT_STATUS = "currentStatus";
    public static final String LAST_LOG = "lastLog";
    private static final Set<String> AVAILABLE_COMMANDS = new HashSet<>(Arrays.asList(START, STOP, CURRENT_STATUS, LAST_LOG));

    private final transient Gson gson = new Gson();

    @Reference
    private transient MultiPackageUpdate updater;

    private transient MultiPackageUpdateServletConfig config;

    private PackagesListEndpoint endpoint;

    @Activate
    public void activate(final MultiPackageUpdateServletConfig config) {
        this.config = config;
        endpoint = new PackagesListEndpoint(config.serverUrl(), config.filename());
    }

    @Override
    protected void doPost(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws IOException {
        final String cmd = request.getParameter(CMD);
        if (!AVAILABLE_COMMANDS.contains(cmd)) {
            return;
        }

        final MultiPackageUpdateResponse result = execute(cmd);
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(gson.toJson(result));
    }

    private MultiPackageUpdateResponse execute(final String cmd) {
        if (StringUtils.equalsIgnoreCase(cmd, START)) {
            return updater.start(endpoint, SUB_SERVICE_NAME, config.maxRetriesCount());
        }

        if (StringUtils.equalsIgnoreCase(cmd, STOP)) {
            return updater.stop();
        }

        if (StringUtils.equalsIgnoreCase(cmd, CURRENT_STATUS)) {
            return updater.getCurrentStatus();
        }

        return updater.getLastLogText();
    }
}