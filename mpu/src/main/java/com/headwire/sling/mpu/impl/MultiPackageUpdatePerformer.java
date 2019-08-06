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

import com.headwire.sling.mpu.MultiPackageUpdate.ServiceStatus;
import com.headwire.sling.mpu.PackagesListEndpoint;
import com.headwire.sling.mpu.ProcessPerformer;
import com.headwire.sling.mpu.ProcessPerformerListener;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.jackrabbit.vault.fs.api.ProgressTrackerListener;
import org.apache.jackrabbit.vault.fs.io.ImportOptions;
import org.apache.jackrabbit.vault.packaging.JcrPackage;
import org.apache.jackrabbit.vault.packaging.JcrPackageManager;
import org.apache.jackrabbit.vault.packaging.PackageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public final class MultiPackageUpdatePerformer implements ProgressTrackerListener, ProcessPerformer {

	public static final String NEW_LINE = "\n";

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

	private final ImportOptions importOptions = new ImportOptions();

	private final PackagesListEndpoint endpoint;
	private final ProcessPerformerListener listener;
	private final JcrPackageManager packageManager;
	private final int maxRetriesCount;

	private boolean terminate = false;
	private boolean appendTerminatedByUserCalled = false;

	public MultiPackageUpdatePerformer(final PackagesListEndpoint endpoint, final JcrPackageManager packageManager, final ProcessPerformerListener listener, final int maxRetriesCount) {
		this.endpoint = endpoint;
		logger.info("Update Performer for Endpoint: '{}'", endpoint);
		this.listener = listener;
		this.packageManager = packageManager;
		this.maxRetriesCount = maxRetriesCount;
		log("Update Runner created @" + dateFormat.format(Calendar.getInstance().getTime()), null);
		importOptions.setListener(this);
	}

	@Override
	public void run() {
		try {
			log("Start Package Installation: " + endpoint.getName(), ServiceStatus.running);
			process();
			log("Finished Package Installation: " + endpoint.getName(), ServiceStatus.installed);
		} catch (final IOException | RepositoryException | PackageException e) {
			final String msg = "Unable to update packages from " + endpoint.getPackagesListUrl();
			log(msg, ServiceStatus.failed);
			logger.error(msg, e);
		}
	}

	private void log(String message, ServiceStatus status) {
		if(message != null) {
			endpoint.log(message);
			logger.info(message);
		}
		if(status != null) {
			endpoint.setStatus(status);
		}
		listener.notifyProcessUpdate(endpoint);
	}

//	private void append(final Object... messages) {
//		for (final Object message : messages) {
//			logText.append(message);
//		}
//	}
//
//	private void appendCurrentTime() {
//		append(dateFormat.format(Calendar.getInstance().getTime()));
//	}
//
//	private void appendNewLine(final Object... messages) {
//		append(messages);
//		append(NEW_LINE);
//	}
//
//	private void endSentence() {
//		appendNewLine(".");
//	}
//
//	private void appendSentence(final Object... messages) {
//		append(messages);
//		endSentence();
//	}
//
//	private void appendStackTrace(final Exception e) {
//		append(ExceptionUtils.getStackTrace(e));
//	}
//
//	private void appendTerminatedByUser() {
//		if (!appendTerminatedByUserCalled) {
//			append("Update Runner terminated by user.");
//		}
//
//		appendTerminatedByUserCalled = true;
//	}

	private void process() throws IOException, RepositoryException, PackageException {
		logger.info("Start Performer Process");
		for (final String packageName : getPackagesNames()) {
			logger.info("Handle Package: '{}'", packageName);
			if (terminate) {
				logger.info("User Terminated");
				log("Update Runner terminated by user", ServiceStatus.terminated);
				return;
			}

			logger.info("Install Package");
			log("Processing package: " + packageName, null);
			installPackageWithRetries(endpoint.getFileUrl(packageName));
		}
	}

	private String[] getPackagesNames() throws IOException {
		final String url = endpoint.getPackagesListUrl();
		log("Downloading packages names from: " + url, null);
		String packagesText = IOUtils.toString(new URL(url), Charsets.UTF_8);
		packagesText = StringUtils.trimToEmpty(packagesText);
		return StringUtils.split(packagesText, NEW_LINE);
	}

	private void installPackageWithRetries(final String url) throws IOException, RepositoryException, PackageException {
		for (int i = 1; i <= maxRetriesCount; i++) {
			if (terminate) {
				log("Update Runner terminated by user", ServiceStatus.terminated);
				return;
			}

			log("Retry installation #: " + i, null);
			try {
				installPackage(url);
				break;
			} catch (final IOException | RepositoryException | PackageException e) {
				if (i >= maxRetriesCount) {
					throw e;
				}
			}
		}
	}

	private void installPackage(final String url) throws IOException, RepositoryException, PackageException {
		log("Downloading & uploading package: " + url, null);
		final InputStream stream = new URL(url).openStream();
		final JcrPackage pack = packageManager.upload(stream, true);
		if (terminate) {
			log("Update Runner terminated by user", null);
			return;
		}

		log("Installing package: " + url, null);
		pack.install(importOptions);
	}

	@Override
	public void onMessage(final Mode mode, final String action, final String path) {
		log("Message, action: " + action + ", path: " + path, null);
	}

	@Override
	public void onError(final Mode mode, final String path, final Exception e) {
		log("Installation caused error, path: '" + path + "', message: " + e.getLocalizedMessage(), ServiceStatus.failed);
	}

	@Override
	public void terminate() {
		terminate = true;
	}

	@Override
	public boolean isTerminated() {
		return terminate;
	}
}
