package com.headwire.sling.mpu.impl;

import com.headwire.sling.mpu.HttpStatusCodeMapper;
import com.headwire.sling.mpu.MultiPackageUpdate.Operation;
import com.headwire.sling.mpu.MultiPackageUpdateResponse.Code;
import org.osgi.service.component.annotations.Component;

import java.util.EnumMap;

import static javax.servlet.http.HttpServletResponse.*;

@Component(service = { HttpStatusCodeMapper.class })
public final class HttpStatusCodeMapperService implements HttpStatusCodeMapper {

    private final EnumMap<Operation, EnumMap<Code, Integer>> mappings = new EnumMap<>(Operation.class);

    public HttpStatusCodeMapperService() {
        for (final Operation operation : Operation.class.getEnumConstants()) {
            mappings.put(operation, new EnumMap<>(Code.class));
        }

        map(Operation.START, Code.SCHEDULED, SC_CREATED);
        map(Operation.START, Code.WAITING, SC_OK);
        map(Operation.START, Code.IN_PROGRESS, SC_OK);

        map(Operation.STOP, Code.AWAITING_TERMINATION, SC_ACCEPTED);
        map(Operation.STOP, Code.TERMINATED, SC_CREATED);
        map(Operation.STOP, Code.UNAVAILABLE, SC_OK);

        map(Operation.CURRENT_STATUS, Code.UNAVAILABLE, SC_OK);
        map(Operation.CURRENT_STATUS, Code.AWAITING_TERMINATION, SC_OK);
        map(Operation.CURRENT_STATUS, Code.IN_PROGRESS, SC_OK);

        map(Operation.LAST_LOG_TEXT, Code.AVAILABLE, SC_OK);
        map(Operation.LAST_LOG_TEXT, Code.UNAVAILABLE, SC_OK);

        map(Operation.LIST, Code.AVAILABLE, SC_OK);
        map(Operation.LIST, Code.UNAVAILABLE, SC_OK);
    }

    private void map(final Operation operation, final Code code, final int result) {
        mappings.get(operation).put(code, result);
    }

    @Override
    public int getStatusCode(final Operation operation, final Code code) {
        return mappings.get(operation).getOrDefault(code, SC_INTERNAL_SERVER_ERROR);
    }

}
