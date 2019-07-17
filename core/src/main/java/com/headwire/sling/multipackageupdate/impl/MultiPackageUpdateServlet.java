package com.headwire.sling.multipackageupdate.impl;

import com.google.gson.Gson;
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
import javax.servlet.Servlet;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.apache.sling.api.servlets.ServletResolverConstants.*;

@Component(service = Servlet.class,
        property = {
                SLING_SERVLET_METHODS + "=POST",
                SLING_SERVLET_RESOURCE_TYPES + "=multipackageupdate/update",
                SLING_SERVLET_SELECTORS + "=json"
        })
@Designate(ocd = MultiPackageUpdateServletConfig.class)
public final class MultiPackageUpdateServlet extends SlingAllMethodsServlet implements PackagesListEndpoint, PackagesUpdatedListener {

    private static final long serialVersionUID = -1704915461516132101L;

    private static final String SUB_SERVICE_NAME = "multipackageupdate";

    private static final String UNABLE_TO_OBTAIN_SESSION = "Unable to obtain session";
    private static final String NO_UPDATE_THREAD_RUNNING_CURRENTLY = "There is no update thread running currently";

    private static final String CMD = "cmd";
    private static final String START = "start";
    private static final String STOP = "stop";
    private static final String CURRENT_STATUS = "currentStatus";
    private static final String LAST_LOG = "lastLog";
    private static final Set<String> AVAILABLE_COMMANDS = new HashSet<>(Arrays.asList(START, STOP, CURRENT_STATUS, LAST_LOG));

    private transient final Logger logger = LoggerFactory.getLogger(getClass());
    private transient final Object lock = new Object();

    private transient final Gson gson = new Gson();

    private MultiPackageUpdateServletConfig config;

    @Reference
    private transient SlingRepository repository;

    private transient MultiPackageUpdateThread currentThread;

    private String lastLogText;

    @Activate
    public void activate(final MultiPackageUpdateServletConfig config) {
        this.config = config;
    }

    @Override
    protected void doPost(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws IOException {
        final String cmd = request.getParameter(CMD);
        if (!AVAILABLE_COMMANDS.contains(cmd)) {
            return;
        }

        final MultiPackageUpdateResponse result = execute(cmd);
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(gson.toJson(result));
    }

    private MultiPackageUpdateResponse execute(final String cmd) {
        if (StringUtils.equalsIgnoreCase(cmd, START)) {
            return startThreadOrGetStatus();
        }

        if (StringUtils.equalsIgnoreCase(cmd, STOP)) {
            synchronized (lock) {
                if (currentThread == null) {
                    return new MultiPackageUpdateResponse(NO_UPDATE_THREAD_RUNNING_CURRENTLY);
                } else {
                    currentThread.terminate();
                    final MultiPackageUpdateResponse response = new MultiPackageUpdateResponse("Update thread marked for earlier termination");
                    response.setLog(currentThread.getLogText());
                    return response;
                }
            }
        }

        if (StringUtils.equalsIgnoreCase(cmd, CURRENT_STATUS)) {
            synchronized (lock) {
                if (currentThread == null) {
                    return new MultiPackageUpdateResponse(NO_UPDATE_THREAD_RUNNING_CURRENTLY);
                } else {
                    final MultiPackageUpdateResponse response = new MultiPackageUpdateResponse("Update process in progress");
                    response.setLog(currentThread.getLogText());
                    return response;
                }
            }
        }

        final String status = StringUtils.isBlank(lastLogText) ? "No previous log available" :  "Last log";
        final MultiPackageUpdateResponse response = new MultiPackageUpdateResponse(status);
        response.setLog(lastLogText);
        return response;
    }

    private MultiPackageUpdateResponse startThreadOrGetStatus() {
        synchronized(lock) {
            if (currentThread == null) {
                return startThread();
            } else {
                final MultiPackageUpdateResponse response = new MultiPackageUpdateResponse("Update process already in progress");
                response.setLog(currentThread.getLogText());
                return response;
            }
        }
    }

    private MultiPackageUpdateResponse startThread() {
        try {
            final Session session = repository.loginService(SUB_SERVICE_NAME, null);
            currentThread = new MultiPackageUpdateThread(this, this, session);
            currentThread.start();
            return new MultiPackageUpdateResponse("Update process started just now");
        } catch (final RepositoryException e) {
            logger.error(UNABLE_TO_OBTAIN_SESSION, e);
            final MultiPackageUpdateResponse response = new MultiPackageUpdateResponse(UNABLE_TO_OBTAIN_SESSION);
            response.setLog(ExceptionUtils.getStackTrace(e));
            return response;
        }
    }

    @Override
    public String getServerUrl() {
        return config.server_url();
    }

    @Override
    public String getFileUrl(final String name) {
        return getServerUrl() + "/" + name;
    }

    @Override
    public String getPackagesListUrl() {
        return getFileUrl(config.filename());
    }

    @Override
    public void notifyPackagesUpdated(final String logText) {
        synchronized(lock) {
            lastLogText = logText;
            currentThread = null;
        }
    }
}