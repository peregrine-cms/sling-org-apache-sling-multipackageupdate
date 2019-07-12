package com.headwire.sling.update.impl;

import com.headwire.sling.update.PackagesListEndpoint;
import com.headwire.sling.update.UpdatePackagesListener;
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

public final class UpdatePackagesThread extends Thread implements ProgressTrackerListener {

	public static final String TERMINATED_BY_USER = "Update process terminated by user.";

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

	private final ImportOptions importOptions = new ImportOptions();

	private final PackagesListEndpoint endpoint;
	private final UpdatePackagesListener listener;
	private final Session session;

	private JcrPackageManager packageManager;

	private String status;

	private boolean terminate = false;

	public UpdatePackagesThread(final PackagesListEndpoint endpoint, final UpdatePackagesListener listener, final Session session) {
		this.endpoint = endpoint;
		this.listener = listener;
		this.session = session;
		status = "Update Packages Thread created @ " + dateFormat.format(Calendar.getInstance().getTime()) + ".\n";
		importOptions.setListener(this);
	}

	@Override
	public void run() {
		packageManager = PackagingService.getPackageManager(session);
		try {
			process();
		} catch (final IOException | RepositoryException | PackageException e) {
			final String msg = "Unable to update packages from " + endpoint.getPackagesListUrl();
			logger.error(msg, e);
			status += msg + "\n" + ExceptionUtils.getStackTrace(e);
		}

		listener.notifyPackagesUpdated(status);
	}

	private void process() throws IOException, RepositoryException, PackageException {
		final String packagesListUrl = endpoint.getPackagesListUrl();
		status += "Downloading packages names from: " + packagesListUrl + ".\n";
		for (final String packageName : getPackagesNames(packagesListUrl)) {
			if (terminate) {
				status += TERMINATED_BY_USER;
				return;
			}

			status += "Downloading package: " + packageName + ".\n";
			final InputStream stream = downloadPackage(packageName);
			final JcrPackage pack = packageManager.upload(stream, true);
			if (terminate) {
				status += TERMINATED_BY_USER;
				return;
			}

			status += "Installing package: " + packageName + ".\n";
			pack.install(importOptions);
		}
	}

	private String[] getPackagesNames(final String url) throws IOException {
		String packagesText = IOUtils.toString(new URL(url), Charsets.UTF_8);
		packagesText = StringUtils.trimToEmpty(packagesText);
		return StringUtils.split(packagesText, "\n");
	}

	private InputStream downloadPackage(final String name) throws IOException {
		final String url = endpoint.getFileUrl(name);
		return new URL(url).openStream();
	}

	@Override
	public void onMessage(final Mode mode, final String action, final String path) {
		status += action + ": " + path + "\n";
	}

	@Override
	public void onError(final Mode mode, final String path, final Exception e) {
		status += "[ERROR] " + path + "\n" + ExceptionUtils.getStackTrace(e) + "\n";
	}

	public String getStatus() {
		return status;
	}

	public void terminate() {
		terminate = true;
	}
}
