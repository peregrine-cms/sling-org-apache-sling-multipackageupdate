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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

import javax.servlet.Servlet;

import static com.headwire.sling.multipackageupdate.MPUConstants.PROJECT_NAME;

@Component(service = Servlet.class,
        property = {
                MultiPackageUpdateServlet.SERVLET_METHODS_PROPERTY_GET,
                MultiPackageUpdateServlet.SERVLET_METHODS_PROPERTY_POST,
                MultiPackageUpdateServlet.RESOURCE_TYPES_PROPERTY_PREFIX + "start",
                MultiPackageUpdateServlet.SERVLET_EXTENSIONS_PROPERTY
        })
@Designate(ocd = MultiPackageUpdateServletConfig.class)
public final class MultiPackageUpdateStartServlet extends MultiPackageUpdateServlet {

    private static final long serialVersionUID = -7049154615161321011L;

    private static final String SUB_SERVICE_NAME = PROJECT_NAME;

    private transient MultiPackageUpdateServletConfig config;

    private PackagesListEndpoint endpoint;

    @Activate
    public void activate(final MultiPackageUpdateServletConfig config) {
        this.config = config;
        endpoint = new PackagesListEndpoint(config.serverUrl(), config.filename());
    }


    @Reference
    private transient MultiPackageUpdate updater;

    @Override
    protected MultiPackageUpdateResponse execute() {
        return updater.start(endpoint, SUB_SERVICE_NAME, config.maxRetriesCount());
    }
}