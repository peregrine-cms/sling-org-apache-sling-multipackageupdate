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

import com.headwire.sling.mpu.MultiPackageUpdateResponse;

import java.util.ArrayList;
import java.util.List;

public final class MultiPackageUpdateResponseImpl implements MultiPackageUpdateResponse {

    private Code code;
    private String action;
    private List<String> details = new ArrayList<>();

    @Override
    public Code getCode() {
        return code;
    }

    public MultiPackageUpdateResponseImpl setCode(Code code) {
        this.code = code;
        return this;
    }

    @Override
    public String getAction() {
        return action;
    }

    public MultiPackageUpdateResponseImpl setAction(String action) {
        this.action = action;
        return this;
    }

    @Override
    public List<String> getDetails() {
        return details;
    }

    public MultiPackageUpdateResponseImpl setDetails(List<String> details) {
        this.details = details;
        return this;
    }
}