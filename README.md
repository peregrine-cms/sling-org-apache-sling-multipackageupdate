### Multi Package Update

#### License

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

#### Introduction

This project delivers a simple servlet `MultiPackageUpdateServlet` that can perform an installation of a given list
of packages.

#### Usage

The servlet is exposed at [/bin/update-packages](http://localhost:8080/bin/update-packages)
and needs a parameter named `cmd`, which can be one of the following:
- [`start`](http://localhost:8080/bin/update-packages?cmd=start): to trigger the multi-package update process,
- [`stop`](http://localhost:8080/bin/update-packages?cmd=stop): to stop the current update thread
(if there is one running),
- [`lastStatus`](http://localhost:8080/bin/update-packages?cmd=lastStatus): to check the last status,
- [`currentStatus`](http://localhost:8080/bin/update-packages?cmd=currentStatus):
to see the current status (if there is a thread running).

#### Configuration

The main required configuration is that of
[com.headwire.sling.multipackageupdate.impl.MultiPackageUpdateServlet](http://localhost:8080/system/console/configMgr/com.headwire.sling.multipackageupdate.impl.MultiPackageUpdateServlet):
- **Server with Packages**: the full `URL` of the location of both packages and the listing file from below.
An example value could be `http://localhost:8080/content`,
- **Packages List File Name**: the name of a text file, located under the above path, containing
the list of packages names to be installed. An example value could be `packages.txt`.

##### Service User Mapping
For the proper operation of the `servlet` one has to configure
[Sling Service User Mapper Service](http://localhost:8080/system/console/configMgr/org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended)
to associate `com.headwire.sling.multipackageupdate:multipackageupdate` with the name of
a [System User](http://localhost:8080/bin/users.html/home/users/system/sling-multipackageupdate) with appropriate
privileges allowing for `package` upload and installation.

#### Build and Installation

The project is built quite simple:

    mvn clean install
    
To install the OSGi bundle use the project **autoInstallBundle**:

    mvn clean install -P autoInstallBundle

To install the Content together with the core bundle
use the project **autoInstallPackage**:

    mvn clean install -P autoInstallPackage

##### ATTENTION

It is probably best not to deploy the OSGi Bundle alone as this
may lead to conflicts with the package deployment.

In case of a misshape the package and bundles needs to be uninstalled
manually:

1. Remove [`/apps/multipackageupdate/install`](http://localhost:8080/bin/browser.html/apps/multipackageupdate/install) folder,
2. Uninstall the package using [the package manager](http://localhost:8080/bin/packages.html),
3. Remove the package from [`/etc/packages`](http://localhost:8080/bin/browser.html/etc/packages) including the snapshots
if they are still there,
4. Remove [the Bundle](http://localhost:8080/system/console/bundles/com.headwire.sling.multipackageupdate) using
the [OSGi Console](http://localhost:8080/system/console/bundles).

