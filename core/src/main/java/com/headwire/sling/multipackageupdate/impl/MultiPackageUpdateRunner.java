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

import com.headwire.sling.multipackageupdate.PackagesListEndpoint;
import com.headwire.sling.multipackageupdate.ProcessPerformerListener;
import com.headwire.sling.multipackageupdate.ProcessPerformer;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.jackrabbit.vault.fs.api.ProgressTrackerListener;
import org.apache.jackrabbit.vault.fs.io.ImportOptions;
import org.apache.jackrabbit.vault.packaging.JcrPackage;
import org.apache.jackrabbit.vault.packaging.JcrPackageManager;
import org.apache.jackrabbit.vault.packaging.PackageException;
import org.apache.jackrabbit.vault.packaging.PackagingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public final class MultiPackageUpdateRunner implements ProgressTrackerListener, ProcessPerformer {

	public static final String TERMINATED_BY_USER = "Update Runner terminated by user.";
	public static final String NEW_LINE = "\n";

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

	private final StringBuilder logText = new StringBuilder("Update Runner created @ ");

	private final ImportOptions importOptions = new ImportOptions();

	private final PackagesListEndpoint endpoint;
	private final ProcessPerformerListener listener;
	private final Session session;

	private JcrPackageManager packageManager;

	private boolean terminate = false;

	public MultiPackageUpdateRunner(final PackagesListEndpoint endpoint, final Session session, final ProcessPerformerListener listener) {
		this.endpoint = endpoint;
		this.listener = listener;
		this.session = session;
		appendCurrentTime();
		endSentence();
		importOptions.setListener(this);
	}

	public void run() {
		packageManager = PackagingService.getPackageManager(session);
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

	private void append(final String... messages) {
		for (final String message : messages) {
			logText.append(message);
		}
	}

	private void appendCurrentTime() {
		append(dateFormat.format(Calendar.getInstance().getTime()));
	}

	private void appendNewLine(final String... messages) {
		append(messages);
		append(NEW_LINE);
	}

	private void endSentence() {
		appendNewLine(".");
	}

	private void appendSentence(final String... messages) {
		append(messages);
		endSentence();
	}

	private void appendStackTrace(final Exception e) {
		append(ExceptionUtils.getStackTrace(e));
	}

	private void process() throws IOException, RepositoryException, PackageException {
		final String packagesListUrl = endpoint.getPackagesListUrl();
		appendSentence("Downloading packages names from: ", packagesListUrl);
		for (final String packageName : getPackagesNames(packagesListUrl)) {
			if (terminate) {
				append(TERMINATED_BY_USER);
				return;
			}

			appendSentence("Downloading package: ", packageName);
			final InputStream stream = downloadPackage(packageName);
			final JcrPackage pack = packageManager.upload(stream, true);
			if (terminate) {
				append(TERMINATED_BY_USER);
				return;
			}

			appendSentence("Installing package: ", packageName);
			pack.install(importOptions);
		}
	}

	private String[] getPackagesNames(final String url) throws IOException {
		String packagesText = IOUtils.toString(new URL(url), Charsets.UTF_8);
		packagesText = StringUtils.trimToEmpty(packagesText);
		return StringUtils.split(packagesText, NEW_LINE);
	}

	private InputStream downloadPackage(final String name) throws IOException {
		final String url = endpoint.getFileUrl(name);
		return new URL(url).openStream();
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

	public String getLogText() {
		return logText.toString();
	}

	public void terminate() {
		terminate = true;
	}
}
