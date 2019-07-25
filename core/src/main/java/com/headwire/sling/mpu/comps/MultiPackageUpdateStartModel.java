package com.headwire.sling.mpu.comps;

import com.headwire.sling.mpu.MultiPackageUpdate;
import com.headwire.sling.mpu.MultiPackageUpdateResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;

@Model(adaptables = Resource.class)
public final class MultiPackageUpdateStartModel extends MultiPackageUpdateModel {

    @Override
    protected MultiPackageUpdateResponse execute(final MultiPackageUpdate updater) {
        return updater.start();
    }

}
