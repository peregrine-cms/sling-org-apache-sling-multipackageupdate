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
import com.headwire.sling.multipackageupdate.MultiPackageUpdateResponse;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;

import java.io.IOException;

import static com.headwire.sling.multipackageupdate.MPUConstants.PROJECT_NAME;
import static com.headwire.sling.multipackageupdate.MPUUtil.*;
import static org.apache.sling.api.servlets.ServletResolverConstants.*;

public abstract class MultiPackageUpdateServlet extends SlingAllMethodsServlet {

    private static final long serialVersionUID = -1704915461516132101L;

    protected static final String SERVLET_METHODS_PROPERTY = SLING_SERVLET_METHODS + EQUALS + POST;
    protected static final String RESOURCE_TYPES_PROPERTY_PREFIX = SLING_SERVLET_RESOURCE_TYPES + EQUALS + PROJECT_NAME + SLASH;
    protected static final String SERVLET_EXTENSIONS_PROPERTY = SLING_SERVLET_EXTENSIONS + EQUALS + JSON;

    private final transient Gson gson = new Gson();

    @Override
    protected void doPost(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws IOException {
        final MultiPackageUpdateResponse result = execute();
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(gson.toJson(result));
    }

    protected abstract MultiPackageUpdateResponse execute();

}