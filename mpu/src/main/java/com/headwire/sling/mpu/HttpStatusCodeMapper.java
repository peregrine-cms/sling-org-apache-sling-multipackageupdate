package com.headwire.sling.mpu;

public interface HttpStatusCodeMapper {

    int getStatusCode(MultiPackageUpdate.Operation operation, MultiPackageUpdateResponse.Code code);

}
