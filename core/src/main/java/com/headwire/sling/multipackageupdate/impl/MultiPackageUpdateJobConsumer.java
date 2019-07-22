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

import com.headwire.sling.multipackageupdate.MultiPackageUpdatePerformerFactory;
import com.headwire.sling.multipackageupdate.PackagesListEndpoint;
import com.headwire.sling.multipackageupdate.ProcessPerformer;
import com.headwire.sling.multipackageupdate.ProcessPerformerListener;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

@Component(immediate = true, service = JobConsumer.class, property = {
		JobConsumer.PROPERTY_TOPICS + "=" + MultiPackageUpdateJobConsumer.TOPIC })
public final class MultiPackageUpdateJobConsumer implements JobConsumer {

	public static final String TOPIC = "com/headwire/sling/multipackageupdate/UPDATE";
	public static final String ENDPOINT = "endpoint";
	public static final String SUB_SERVICE_NAME = "subServiceName";
	public static final String MAX_RETRIES_COUNT = "maxRetriesCount";

	private static final String UNABLE_TO_OBTAIN_SESSION = "Unable to obtain session";

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Reference
	private SlingRepository repository;

	@Reference
	private MultiPackageUpdatePerformerFactory multiPackageUpdatePerformerFactory;

	@Reference
	private ProcessPerformerListener processPerformerListener;

	@Override
	public JobResult process(final Job job) {
		final PackagesListEndpoint endpoint = job.getProperty(ENDPOINT, PackagesListEndpoint.class);
		final String subServiceName = job.getProperty(SUB_SERVICE_NAME, String.class);
		final int maxRetriesCount = job.getProperty(MAX_RETRIES_COUNT, 1);
		try {
			final Session session = repository.loginService(subServiceName, null);
			final ProcessPerformer performer = multiPackageUpdatePerformerFactory.createPerformer(endpoint, session, processPerformerListener, maxRetriesCount);
			if (processPerformerListener.setProcessPerformer(performer)) {
				performer.run();
			}

			return JobResult.OK;
		} catch (final RepositoryException e) {
			logger.error(UNABLE_TO_OBTAIN_SESSION, e);
			processPerformerListener.notifyProcessFinished(ExceptionUtils.getStackTrace(e));

			return JobResult.CANCEL;
		}
	}

}
