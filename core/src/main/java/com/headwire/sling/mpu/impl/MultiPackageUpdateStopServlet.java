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

import com.headwire.sling.mpu.HttpStatusCodeMapper;
import com.headwire.sling.mpu.MultiPackageUpdate;
import com.headwire.sling.mpu.MultiPackageUpdateResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletResponse;

@Component(service = Servlet.class,
        property = {
                MultiPackageUpdateServlet.SERVLET_METHODS_PROPERTY_GET,
                MultiPackageUpdateServlet.SERVLET_METHODS_PROPERTY_POST,
                MultiPackageUpdateServlet.RESOURCE_TYPES_PROPERTY_PREFIX + "stop",
                MultiPackageUpdateServlet.SERVLET_EXTENSIONS_PROPERTY
        })
public final class MultiPackageUpdateStopServlet extends MultiPackageUpdateServlet {

    private static final long serialVersionUID = -491546151613210117L;


    @Reference
    private transient MultiPackageUpdate updater;

    @Reference
    private transient HttpStatusCodeMapper httpMapper;

    @Override
    protected MultiPackageUpdateResponse execute() {
        return updater.stop();
    }

    @Override
    protected int getStatusCode(final MultiPackageUpdateResponse.Code code) {
        return httpMapper.getStatusCode(MultiPackageUpdate.Operation.STOP, code);
    }
}