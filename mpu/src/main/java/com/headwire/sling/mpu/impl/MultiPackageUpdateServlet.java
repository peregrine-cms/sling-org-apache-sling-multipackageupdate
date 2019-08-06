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

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.headwire.sling.mpu.MultiPackageUpdateResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import static com.headwire.sling.mpu.MPUConstants.PROJECT_NAME;
import static com.headwire.sling.mpu.MPUUtil.*;
import static org.apache.sling.api.servlets.ServletResolverConstants.*;

public abstract class MultiPackageUpdateServlet extends SlingAllMethodsServlet {

    private static final long serialVersionUID = -1704915461516132101L;

    protected static final String SERVLET_METHODS_PROPERTY_GET = SLING_SERVLET_METHODS + EQUAL + GET;
    protected static final String SERVLET_METHODS_PROPERTY_POST = SLING_SERVLET_METHODS + EQUAL + POST;
    protected static final String RESOURCE_TYPES_PROPERTY_PREFIX = SLING_SERVLET_RESOURCE_TYPES + EQUAL + PROJECT_NAME + SLASH + COMPONENTS + SLASH;
    protected static final String SERVLET_JSON_EXTENSIONS_PROPERTY = SLING_SERVLET_EXTENSIONS + EQUAL + JSON;
    protected static final String SERVLET_HTML_EXTENSIONS_PROPERTY = SLING_SERVLET_EXTENSIONS + EQUAL + HTML;

    protected enum TYPE { json, html };
    protected static final List<TYPE> JSON_HTML_TYPES = Arrays.asList(new TYPE[] {TYPE.json, TYPE.html});
    protected static final List<TYPE> JSON_TYPES = Arrays.asList(new TYPE[] {TYPE.json});
    protected static final List<TYPE> HTML_TYPES = Arrays.asList(new TYPE[] {TYPE.html});

    private static final transient ObjectMapper objectMapper;
    private static final transient DefaultPrettyPrinter prettyPrinter;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        prettyPrinter = new DefaultPrettyPrinter();
        prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
    }

    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws IOException {
        final PrintWriter writer = response.getWriter();
        String name = request.getRequestPathInfo().getSuffix();
        while(name != null && !name.isEmpty() && name.charAt(0) == '/') {
            if (name.length() == 1) {
                name = "";
            } else {
                name = name.substring(1);
            }
        }
        logger.info("Called doGet(), request suffix: '{}'", name);
        final MultiPackageUpdateResponse result = execute(name);
        if (acceptsType(request, TYPE.json)) {
            logger.info("Handle JSon Call");
            response.setContentType(APPLICATION_JSON);
            response.setCharacterEncoding(UTF_8);
            final int statusCode = getStatusCode(result.getCode());
            logger.info("Json Status Code: '{}'", statusCode);
            response.setStatus(statusCode);
            String json = objectMapper
                .writer(prettyPrinter)
                .writeValueAsString(result);
            logger.info("JSon Output: '{}'", json);
            writer.println(json);
        } else if (acceptsType(request, TYPE.html)) {
            logger.info("Handle HTML Call");
            response.setContentType(TEXT_HTML);
            response.setCharacterEncoding(UTF_8);
            final int statusCode = getStatusCode(result.getCode());
            logger.info("HTML Status Code: '{}'", statusCode);
            response.setStatus(statusCode);
            String html = "<html><head><title>Log Output</title></head><body><h2>Log Statements:</h2>";
            List<String> logs = result.getDetails();
            if (logs.isEmpty()) {
                logs.add("No Log Statements Found");
            }
            for(String log: logs) {
                html += "<p>" + log + "</p>";
            }
            html += "</html>";
            writer.println(html);
            logger.info("HTML Output: '{}'", html);
        } else {
            logger.info("Handle unsupported Call");
            response.setContentType(TEXT_PLAIN);
            response.setCharacterEncoding(UTF_8);
            response.setStatus(500);
            writer.println("This extension or accepted content is not supported. Please use on of these types: " + getSupportedTypes());
        }
        writer.flush();
        writer.close();
        logger.info("DONE doGet() call");
    }

    @Override
    protected void doPost(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws IOException {
        doGet(request, response);
    }

    private boolean acceptsType(final SlingHttpServletRequest request, TYPE type) {
        if(getSupportedTypes().contains(type)) {
            final RequestPathInfo pathInfo = request.getRequestPathInfo();
            final String extension = pathInfo.getExtension();
            String typeExtension = type == TYPE.json ? JSON :
                type == TYPE.html ? HTML : "";
            if (StringUtils.equalsIgnoreCase(extension, typeExtension)) {
                return true;
            }

            String typeAccept = type == TYPE.json ? APPLICATION_JSON :
                type == TYPE.html ? TEXT_HTML : "";
            final Enumeration<?> acceptHeaders = request.getHeaders(ACCEPT);
            while (acceptHeaders.hasMoreElements()) {
                final String element = String.valueOf(acceptHeaders.nextElement());
                if (StringUtils.equalsIgnoreCase(element, typeAccept)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected abstract MultiPackageUpdateResponse execute(String name);

    protected abstract int getStatusCode(MultiPackageUpdateResponse.Code code);

    protected abstract List<TYPE> getSupportedTypes();
}