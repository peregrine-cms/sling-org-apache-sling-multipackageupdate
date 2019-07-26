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

import com.google.gson.Gson;
import com.headwire.sling.mpu.MultiPackageUpdateResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import static com.headwire.sling.mpu.MPUConstants.PROJECT_NAME;
import static com.headwire.sling.mpu.MPUUtil.*;
import static org.apache.sling.api.servlets.ServletResolverConstants.*;

public abstract class MultiPackageUpdateServlet extends SlingAllMethodsServlet {

    private static final long serialVersionUID = -1704915461516132101L;

    protected static final String SERVLET_METHODS_PROPERTY_GET = SLING_SERVLET_METHODS + EQUAL + GET;
    protected static final String SERVLET_METHODS_PROPERTY_POST = SLING_SERVLET_METHODS + EQUAL + POST;
    protected static final String RESOURCE_TYPES_PROPERTY_PREFIX = SLING_SERVLET_RESOURCE_TYPES + EQUAL + PROJECT_NAME + SLASH + COMPONENTS + SLASH;
    protected static final String SERVLET_EXTENSIONS_PROPERTY = SLING_SERVLET_EXTENSIONS + EQUAL + JSON;

    private final transient Gson gson = new Gson();

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws IOException {
        final MultiPackageUpdateResponse result = execute();
        if (acceptsJson(request)) {
            response.setContentType(APPLICATION_JSON);
            response.setCharacterEncoding(UTF_8);
            final PrintWriter writer = response.getWriter();
            final String string = gson.toJson(result);
            writer.write(string);
            final int statusCode = getStatusCode(result.getCode());
            response.setStatus(statusCode);
        }
    }

    @Override
    protected void doPost(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws IOException {
        doGet(request, response);
    }

    private boolean acceptsJson(final SlingHttpServletRequest request) {
        final RequestPathInfo pathInfo = request.getRequestPathInfo();
        final String extension = pathInfo.getExtension();
        if (StringUtils.equalsIgnoreCase(extension, JSON)) {
            return true;
        }

        final Enumeration<?> acceptHeaders = request.getHeaders(ACCEPT);
        while (acceptHeaders.hasMoreElements()) {
            final String element = String.valueOf(acceptHeaders.nextElement());
            if (StringUtils.equalsIgnoreCase(element, APPLICATION_JSON)) {
                return true;
            }
        }

        return false;
    }

    protected abstract MultiPackageUpdateResponse execute();

    protected abstract int getStatusCode(MultiPackageUpdateResponse.Code code);

}