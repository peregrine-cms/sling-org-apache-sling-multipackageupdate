package com.headwire.sling.mpu.comps;

import com.headwire.sling.mpu.MultiPackageUpdate.Operation;
import com.headwire.sling.mpu.MultiPackageUpdateResponse;
import com.headwire.sling.mpu.MultiPackageUpdateResponse.Code;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;

@Model(adaptables = SlingHttpServletRequest.class)
public final class MultiPackageUpdateStopModel extends MultiPackageUpdateModel {

    @Override
    protected MultiPackageUpdateResponse executeImpl(String name) {
        return updater.stop(name);
    }

    @Override
    protected int getStatusCode(final Code code) {
        return httpMapper.getStatusCode(Operation.STOP, code);
    }

}
