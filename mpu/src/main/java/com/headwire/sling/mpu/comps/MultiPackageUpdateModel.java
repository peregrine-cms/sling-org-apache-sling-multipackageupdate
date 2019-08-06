package com.headwire.sling.mpu.comps;

import com.headwire.sling.mpu.HttpStatusCodeMapper;
import com.headwire.sling.mpu.MultiPackageUpdate;
import com.headwire.sling.mpu.MultiPackageUpdateResponse;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;

import javax.inject.Inject;

public abstract class MultiPackageUpdateModel {

    @OSGiService
    protected MultiPackageUpdate updater;

    @OSGiService
    protected HttpStatusCodeMapper httpMapper;

    @Inject
    private SlingHttpServletRequest request;

    @Inject
    private SlingHttpServletResponse response;

    public final MultiPackageUpdateResponse execute() {
        String name = request.getRequestPathInfo().getSuffix();
        while(name != null && !name.isEmpty() && name.charAt(0) == '/') {
            if (name.length() == 1) {
                name = "";
            } else {
                name = name.substring(1);
            }
        }
        final MultiPackageUpdateResponse result = executeImpl(name);
        response.setStatus(getStatusCode(result.getCode()));
        return result;
    }

    protected abstract MultiPackageUpdateResponse executeImpl(String name);

    protected abstract int getStatusCode(MultiPackageUpdateResponse.Code code);

}
