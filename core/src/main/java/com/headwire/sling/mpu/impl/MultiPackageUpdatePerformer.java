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

	private final StringBuilder logText = new StringBuilder("Update Runner created @ ");

	private final ImportOptions importOptions = new ImportOptions();

	private final PackagesListEndpoint endpoint;
	private final ProcessPerformerListener listener;
	private final JcrPackageManager packageManager;
	private final int maxRetriesCount;

	private boolean terminate = false;
	private boolean appendTerminatedByUserCalled = false;

	public MultiPackageUpdatePerformer(final PackagesListEndpoint endpoint, final JcrPackageManager packageManager, final ProcessPerformerListener listener, final int maxRetriesCount) {
		this.endpoint = endpoint;
		this.listener = listener;
		this.packageManager = packageManager;
		this.maxRetriesCount = maxRetriesCount;
		appendCurrentTime();
		endSentence();
		importOptions.setListener(this);
	}

	@Override
	public void run() {
		try {
			process();
		} catch (final IOException | RepositoryException | PackageException e) {
			final String msg = "Unable to update packages from " + endpoint.getPackagesListUrl();
			logger.error(msg, e);
			appendNewLine(msg);
			appendStackTrace(e);
		}

		listener.notifyProcessFinished(getLogText());
	}

	private void append(final Object... messages) {
		for (final Object message : messages) {
			logText.append(message);
		}
	}

	private void appendCurrentTime() {
		append(dateFormat.format(Calendar.getInstance().getTime()));
	}

	private void appendNewLine(final Object... messages) {
		append(messages);
		append(NEW_LINE);
	}

	private void endSentence() {
		appendNewLine(".");
	}

	private void appendSentence(final Object... messages) {
		append(messages);
		endSentence();
	}

	private void appendStackTrace(final Exception e) {
		append(ExceptionUtils.getStackTrace(e));
	}

	private void appendTerminatedByUser() {
		if (!appendTerminatedByUserCalled) {
			append("Update Runner terminated by user.");
		}

		appendTerminatedByUserCalled = true;
	}

	private void process() throws IOException, RepositoryException, PackageException {
		for (final String packageName : getPackagesNames()) {
			if (terminate) {
				appendTerminatedByUser();
				return;
			}

			appendSentence("Processing package: ", packageName);
			installPackageWithRetries(endpoint.getFileUrl(packageName));
		}
	}

	private String[] getPackagesNames() throws IOException {
		final String url = endpoint.getPackagesListUrl();
		appendSentence("Downloading packages names from: ", url);
		String packagesText = IOUtils.toString(new URL(url), Charsets.UTF_8);
		packagesText = StringUtils.trimToEmpty(packagesText);
		return StringUtils.split(packagesText, NEW_LINE);
	}

	private void installPackageWithRetries(final String url) throws IOException, RepositoryException, PackageException {
		for (int i = 1; i <= maxRetriesCount; i++) {
			if (terminate) {
				appendTerminatedByUser();
				return;
			}

			appendSentence("Attempt: ", i);
			try {
				installPackage(url);
				break;
			} catch (final IOException | RepositoryException | PackageException e) {
				if (i < maxRetriesCount) {
					appendStackTrace(e);
				} else {
					throw e;
				}
			}
		}
	}

	private void installPackage(final String url) throws IOException, RepositoryException, PackageException {
		appendSentence("Downloading & uploading package: ", url);
		final InputStream stream = new URL(url).openStream();
		final JcrPackage pack = packageManager.upload(stream, true);
		if (terminate) {
			appendTerminatedByUser();
			return;
		}

		appendSentence("Installing package: ", url);
		pack.install(importOptions);
	}

	@Override
	public void onMessage(final Mode mode, final String action, final String path) {
		appendNewLine(action, ": ", path);
	}

	@Override
	public void onError(final Mode mode, final String path, final Exception e) {
		appendNewLine("[ERROR] ", path);
		appendStackTrace(e);
		appendNewLine();
	}

	@Override
	public String getLogText() {
		return logText.toString();
	}

	@Override
	public void terminate() {
		terminate = true;
	}
}
