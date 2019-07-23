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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;

@Component(service = Servlet.class,
        property = {
                MultiPackageUpdateServlet.SERVLET_METHODS_PROPERTY,
                MultiPackageUpdateServlet.RESOURCE_TYPES_PROPERTY_PREFIX + "status",
                MultiPackageUpdateServlet.SERVLET_EXTENSIONS_PROPERTY
        })
public final class MultiPackageUpdateStatusServlet extends MultiPackageUpdateServlet {

    private static final long serialVersionUID = -9154615161321011704L;


    @Reference
    private transient MultiPackageUpdate updater;

    @Override
    protected MultiPackageUpdateResponse execute() {
        return updater.getCurrentStatus();
    }
}