package com.headwire.sling.update.impl;

import com.headwire.sling.update.PackagesListEndpoint;
import com.headwire.sling.update.UpdatePackagesListener;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import java.io.IOException;

@Component(service = Servlet.class)
@Designate(ocd = UpdatePackagesServletConfig.class)
public final class UpdatePackagesServlet extends SlingAllMethodsServlet implements PackagesListEndpoint, UpdatePackagesListener {

    private static final long serialVersionUID = -1704915461516132101L;

    private static final String DEFAULT_SUB_SERVICE_NAME = "updater";
    public static final String CMD_INFO = "Please add a `cmd` parameter:\n" +
            "- `start`: to trigger the update process,\n" +
            "- `stop`: to stop current update thread,\n" +
            "- `lastStatus`: to check the last status,\n" +
            "- `currentStatus`: to see the current status, if update is running.\n\n";
    public static final String UNABLE_TO_OBTAIN_SESSION = "Unable to obtain session.";
    public static final String NO_UPDATE_THREAD_RUNNING_CURRENTLY = "There is no update thread running currently.";

    private transient final Logger logger = LoggerFactory.getLogger(getClass());
    private transient final Object lock = new Object();

    private UpdatePackagesServletConfig config;

    @Reference
    private transient SlingRepository repository;

    private transient UpdatePackagesThread currentThread;

    private String lastStatus = "No previous status available.";

    @Activate
    public void activate(final UpdatePackagesServletConfig config) {
        this.config = config;
    }

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws IOException {
        final String cmd = request.getParameter("cmd");
        final StringBuilder status = new StringBuilder(CMD_INFO);
        if (StringUtils.equalsIgnoreCase(cmd, "start")) {
            status.append(startThreadOrGetStatus());
        } else if (StringUtils.equalsIgnoreCase(cmd, "stop")) {
            synchronized (lock) {
                if (currentThread == null) {
                    status.append(NO_UPDATE_THREAD_RUNNING_CURRENTLY);
                } else {
                    currentThread.terminate();
                    status.append("Update thread marked for earlier termination.\n" + currentThread.getStatus());
                }
            }
        } else if (StringUtils.equalsIgnoreCase(cmd, "lastStatus")) {
            status.append(lastStatus);
        } else if (StringUtils.equalsIgnoreCase(cmd, "currentStatus")) {
            synchronized (lock) {
                if (currentThread == null) {
                    status.append(NO_UPDATE_THREAD_RUNNING_CURRENTLY);
                } else {
                    status.append(currentThread.getStatus());
                }
            }
        }

        response.getWriter().write(status.toString());
    }

    private String startThreadOrGetStatus() {
        String status = "Update process already in progress:\n";
        synchronized(lock) {
            if (currentThread == null) {
                status = startThread();
            } else {
                status += currentThread.getStatus();
            }
        }

        return status;
    }

    private String startThread() {
        try {
            final Session session = repository.loginService(getSubServiceName(), null);
            currentThread = new UpdatePackagesThread(this, this, session);
            currentThread.start();
            return "Update process started just now.";
        } catch (final RepositoryException e) {
            logger.error(UNABLE_TO_OBTAIN_SESSION, e);
            return UNABLE_TO_OBTAIN_SESSION + "\n" + ExceptionUtils.getStackTrace(e);
        }
    }

    private String getSubServiceName() {
        return StringUtils.defaultIfBlank(config.subservice(), DEFAULT_SUB_SERVICE_NAME);
    }

    public String getServerUrl() {
        return config.server_url();
    }

    public String getFileUrl(final String name) {
        return getServerUrl() + "/" + name;
    }

    public String getPackagesListUrl() {
        return getFileUrl(config.filename());
    }

    @Override
    public void notifyPackagesUpdated(final String status) {
        synchronized(lock) {
            lastStatus = status;
            currentThread = null;
        }
    }
}