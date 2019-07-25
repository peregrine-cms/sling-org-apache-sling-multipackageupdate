package com.headwire.sling.mpu.impl;

import com.headwire.sling.mpu.HttpStatusCodeMapper;
import com.headwire.sling.mpu.MultiPackageUpdate.Operation;
import com.headwire.sling.mpu.MultiPackageUpdateResponse.Code;
import org.osgi.service.component.annotations.Component;

import static javax.servlet.http.HttpServletResponse.*;

@Component(service = { HttpStatusCodeMapper.class })
public final class HttpStatusCodeMapperService implements HttpStatusCodeMapper {

    @Override
    public int getStatusCode(final Operation operation, final Code code) {
        switch (operation) {
            case START: return getStatusCodeForStartOperation(code);
            case STOP: return getStatusCodeForStopOperation(code);
            case CURRENT_STATUS: return getStatusCodeForStatusOperation(code);
            case LAST_LOG_TEXT: return getStatusCodeForLogOperation(code);
        }

        return SC_INTERNAL_SERVER_ERROR;
    }

    public int getStatusCodeForStartOperation(final Code code) {
        if (code == Code.SCHEDULED) {
            return SC_CREATED;
        }

        return SC_OK;
    }

    public int getStatusCodeForStopOperation(final Code code) {
        switch (code) {
            case AWAITING_TERMINATION: return SC_ACCEPTED;
            case TERMINATED: return SC_CREATED;
        }

        return SC_NO_CONTENT;
    }

    public int getStatusCodeForStatusOperation(final Code code) {
        if (code == Code.UNAVAILABLE) {
            return SC_NO_CONTENT;
        }

        return SC_OK;
    }

    public int getStatusCodeForLogOperation(final Code code) {
        if (code == Code.AVAILABLE) {
            return SC_OK;
        }

        return SC_NO_CONTENT;
    }

}
