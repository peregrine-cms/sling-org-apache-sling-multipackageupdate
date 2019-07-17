package com.headwire.sling.multipackageupdate.impl;

import com.headwire.sling.multipackageupdate.PackagesListEndpoint;
import com.headwire.sling.multipackageupdate.PackagesUpdatedListener;
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
import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.Servlet;
import java.io.IOException;

@Component(service = Servlet.class)
@Designate(ocd = MultiPackageUpdateServletConfig.class)
public final class MultiPackageUpdateServlet extends SlingAllMethodsServlet implements PackagesListEndpoint, PackagesUpdatedListener {

    private static final long serialVersionUID = -1704915461516132101L;

    private static final String SUB_SERVICE_NAME = "multipackageupdate";

    private static final String UNABLE_TO_OBTAIN_SESSION = "Unable to obtain session.";
    private static final String NO_UPDATE_THREAD_RUNNING_CURRENTLY = "There is no update thread running currently.";

    private static final String CMD = "cmd";
    private static final String START = "start";
    private static final String STOP = "stop";
    private static final String LAST_STATUS = "lastStatus";
    private static final String CURRENT_STATUS = "currentStatus";

    private transient final Logger logger = LoggerFactory.getLogger(getClass());
    private transient final Object lock = new Object();

    private MultiPackageUpdateServletConfig config;

    @Reference
    private transient SlingRepository repository;

    private transient MultiPackageUpdateThread currentThread;

    private String lastStatus = "No previous status available.";

    @Activate
    public void activate(final MultiPackageUpdateServletConfig config) {
        this.config = config;
    }

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws IOException {
        final String cmd = request.getParameter(CMD);
        final StringBuilder log = new StringBuilder();
        if (StringUtils.equalsIgnoreCase(cmd, START)) {
            log.append(startThreadOrGetStatus());
        } else if (StringUtils.equalsIgnoreCase(cmd, STOP)) {
            synchronized (lock) {
                if (currentThread == null) {
                    log.append(NO_UPDATE_THREAD_RUNNING_CURRENTLY);
                } else {
                    currentThread.terminate();
                    log.append("Update thread marked for earlier termination.\n" + currentThread.getStatus());
                }
            }
        } else if (StringUtils.equalsIgnoreCase(cmd, LAST_STATUS)) {
            log.append(lastStatus);
        } else if (StringUtils.equalsIgnoreCase(cmd, CURRENT_STATUS)) {
            synchronized (lock) {
                if (currentThread == null) {
                    log.append(NO_UPDATE_THREAD_RUNNING_CURRENTLY);
                } else {
                    log.append(currentThread.getStatus());
                }
            }
        }

        final JsonObject json = Json.createObjectBuilder().build();
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(log.toString());
    }

    @Override
    protected void doPost(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws IOException {
        doGet(request, response);
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
            final Session session = repository.loginService(SUB_SERVICE_NAME, null);
            currentThread = new MultiPackageUpdateThread(this, this, session);
            currentThread.start();
            return "Update process started just now.";
        } catch (final RepositoryException e) {
            logger.error(UNABLE_TO_OBTAIN_SESSION, e);
            return UNABLE_TO_OBTAIN_SESSION + "\n" + ExceptionUtils.getStackTrace(e);
        }
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