### Multi Package Update (MPU)

#### Introduction

This project delivers a simple servlet mechanism to perform an installation
of a given list of packages provided by either a local or remote server.

The MPU service allows the provider of package to make it very easy for
a client to install a set of packages into its Sling instance to make it
fully functional. Here, for example,  we showcase how to install the **Peregrine CMS**
application with a few simple steps.

The MPU has two *components*:
1. MPU Configurations which define the Package Provider Endpoints
2. A set of actions than can be invoked onto Configurations to get
   information about them, start of stop the installation
    
MPU comes with two sample use cases (pre-configured in the ui.content package):

1. Local, in-Sling test package
2. Installing **Peregrine CMS** from Peregrine Package Service: **https://vagrant.headwire.com/peregrine/packages.txt**

This can be installed with: `mvn clean install -P autoInstallContentPackage`

#### Setup

After the installation of the MPU the only actions that needs to be done
is to configure package provider endpoints and then start the installation
of that endpoint.

##### Configuration

The MPU Configuration is a Factory allowing a user to configured multiple
Package Provider Endpoints and then install them later.

A new Configuration can be created with:
[com.headwire.sling.mpu.impl.MultiPackageUpdateServiceConfig][1]:

If the MPU ui.content package is installed this would be its configuration
of the **local** Endpoint:

![Local-MultiPackageUpdateService][local]

The Peregrine CMS Endpoint looks like this:

![CMS-MultiPackageUpdateService][peregrine]

The Configuration takes these parameters:

- **Configuration Name**: a unique name of this configuration. If two with the
  same name are defined then the second will replace the first
- **Server with Packages**: the full `URL` of the location of both packages and the listing file from below.
An example value could be `http://localhost:8080/content`,
- **Packages List File Name**: the name of a text file, located under the above path, containing
the list of packages names to be installed. An example value could be `packages.txt`,
- **Max Retries**: maximum number of retries to install a single package.

##### Execution

With the exception of **list** all the action that the **Configuration Name**
as a suffix. List takes none and will list all configured package provider
endpoints:

`curl -u admin:admin http://localhost:8080/bin/mpu/list.json`

This yields this:

```json
{
  "code" : "AVAILABLE",
  "action" : "Endpoint List: ",
  "details" : [
    "peregrine",
    "local"
  ]
}
```

In order to install [Peregrine CMS][cms-home] we need to configure MPU
as laid out in the 2nd configuration above. Now we can proceed to the
installation of it:

1. Check that MPU is ready with `curl -u admin:admin http://localhost:8080/bin/mpu/status.json/peregrine`.
the output would look like this:

```json
{
  "code" : "AVAILABLE",
  "action" : "Status: peregrine",
  "details" : [
    "Status for Endpoint: 'peregrine': 'configured'"
  ]
}
```

##### Installation

The installation is started with the **start** command which will then
download the packages and install them in the background. This means that
the logs will change will the installation is running. Also a restart will
clear the old logs.

`curl -u admin:admin http://localhost:8080/bin/mpu/start.json/peregrine`

This will produce this output:

```json
{
  "code" : "SCHEDULED",
  "action" : "Update job scheduled just now",
  "details" : [ ]
}
```
Use **log.json** or **status.json** to check on the progress and see when
the installation is done.

`curl -u admin:admin http://localhost:8080/bin/mpu/status.json/peregrine`

This will produce this output:

```json
{
  "code" : "AVAILABLE",
  "action" : "Status: peregrine",
  "details" : [
    "Status for Endpoint: 'peregrine': 'installed'"
  ]
}
```

#### Usage

The servlets are exposed under [/bin/mpu](http://localhost:8080/bin/mpu.html) and are the following:

* [`/bin/mpu/list.json`](http://localhost:8080/bin/mpu/list.json): lists all available configurations / endpoints,
* **/bin/mpu/start.json/&lt;configuration name>**: to trigger the multi-package update process of the endpoint,
* **/bin/mpu/stop.json/&lt;configuration name>**: to stop the update thread of the endpoint (if there is one running),
* **/bin/mpu/status.json/&lt;configuration name>**: to see the status of that endpoint
* **/bin/mpu/log.json/&lt;configuration name>**: to check the last log messages of that endpoint (resets when an update is started)

The services are available as HTML and JSON for status and log and only as JSON for start and stop.

##### Service User Mapping

The Service User and Service User Mapping is made through the MPU Bundle
installation. It is documented here for the reader's understanding:

For the proper operation of the `servlet` one has to configure
[Sling Service User Mapper Service](http://localhost:8080/system/console/configMgr/org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended)
to associate `com.headwire.sling.multipackageupdate:multipackageupdate` with the name of
a [System User](http://localhost:8080/bin/users.html/home/users/system/sling-multipackageupdate) with appropriate
privileges allowing for `package` upload and installation.

##### Packages List File

The packages list file is a simple text file containing packages names or `URL`s in each line, e.g.

    my-app-package-1.0.0.zip
    http://localhost:8080/content/multipackageupdate/my-content-package-1.0.0.zip
    https://localhost:8080/content/multipackageupdate/my-acl-package-1.0.0.zip

##### Sample Configuration

The module `ui.content` contains a full set of test files and configurations for both a
local Endpoint and the Peregrine CMS Endpoint.

#### Build and Installation

The project is built quite simple:

    mvn clean install
    
To install the OSGi bundle use the profile `autoInstallBundle`:

    mvn clean install -P autoInstallBundle

To install the sample configuration and content use the profile `autoInstallContentPackage`:

    mvn clean install -P autoInstallContentPackage

##### ATTENTION

Keep in mind that both the status and the logs of every Endpoint is reset
during a deployment of the MPU.

[1]: http://localhost:8080/system/console/configMgr/com.headwire.sling.mpu.impl.MultiPackageUpdateServiceConfig

[local]: docs/img/MultiPackageUpdateService.png
[peregrine]: docs/img/CMS-MultiPackageUpdateService.png