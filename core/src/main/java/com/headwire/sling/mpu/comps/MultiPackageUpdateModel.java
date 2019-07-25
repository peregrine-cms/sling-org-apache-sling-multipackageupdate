package com.headwire.sling.mpu.comps;

import com.headwire.sling.mpu.HttpStatusCodeMapper;
import com.headwire.sling.mpu.MultiPackageUpdate;
import com.headwire.sling.mpu.MultiPackageUpdateResponse;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;

import javax.inject.Inject;

public abstract class MultiPackageUpdateModel {

    @OSGiService
    protected MultiPackageUpdate updater;

    @OSGiService
    protected HttpStatusCodeMapper httpMapper;

    @Inject
    private SlingHttpServletResponse response;

    public final MultiPackageUpdateResponse execute() {
        final MultiPackageUpdateResponse result = executeImpl();
        response.setStatus(getStatusCode(result.getCode()));
        return result;
    }

    protected abstract MultiPackageUpdateResponse executeImpl();

    protected abstract int getStatusCode(MultiPackageUpdateResponse.Code code);

}
