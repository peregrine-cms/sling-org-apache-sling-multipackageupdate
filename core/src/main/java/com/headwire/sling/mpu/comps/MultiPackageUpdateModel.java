package com.headwire.sling.mpu.comps;

import com.headwire.sling.mpu.MultiPackageUpdate;
import com.headwire.sling.mpu.MultiPackageUpdateResponse;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;

public abstract class MultiPackageUpdateModel {

    @OSGiService
    private MultiPackageUpdate updater;

    public final MultiPackageUpdateResponse execute() {
        return execute(updater);
    }

    protected abstract MultiPackageUpdateResponse execute(final MultiPackageUpdate updater);

}
