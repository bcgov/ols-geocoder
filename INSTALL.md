# Open Location Service Geocoder Service Installation


The Open Location Service (OLS) Geocoder Service is a Java 11 web application which runs on the Tomcat 9 Java application server.
The application contains two servers. 1) the main API server for geocoding and 2) an admin server for configuration.
It also requires the following dependencies to be installed:


- Java 11 (OpenJDK 11 with HotSpot JVM recommended) runtime
- Apache Tomcat 9.0+
- Apache Maven 3.6+
- [ols-util library](https://github.com/bcgov/ols-util.git)
- Apache Cassandra (only if you want to use it to save configurations)

This guide describes how to install and configure Geocoder applications, including its admin server in Ubuntu.
In Windows (using WSL) and macOS (using terminal) the process is very similar.

## Table of Contents

- [Install Java runtime](#install-java-runtime)
- [Install and configure Tomcat](#install-and-configure-tomcat)
  - [Download and install Tomcat](#download-and-install-tomcat)
  - [Configuring systemD service](#configuring-systemd-service)
  - [Configuring Tomcat admin console](#configuring-tomcat-admin-console)
- [Install Apache Maven](#install-apache-maven)
  - [Configuring Maven](#configuring-maven)
- [Install Geocoder](#install-geocoder)
    - [Install ols-util dependency](#controller-filters-and-helpers)
    - [Install Geocoder](#configuring-models)
    - [Configuring geocoder](#configuring-geocoder)
    - [Main configuration](#main-configuration)
    - [Data store configuration](#data-store-configuration)
- [Troubleshooting](#troubleshooting)




## Install Java runtime

Geocoder is built using Java. 
Java 11+ (OpenJDK 11+ with HotSpot JVM recommended) is required to compile and run the application.
To test if you have Java runtime installed using the following command:

```console
$ java -version
openjdk version "13.0.1" 2019-10-15
OpenJDK Runtime Environment (build 13.0.1+9)
OpenJDK 64-Bit Server VM (build 13.0.1+9, mixed mode, sharing)
```

if not you can install java using following commands. 
Windows users may download an installer at https://www.oracle.com/java/technologies/downloads/#java17.


```console
$ sudo apt update
$ sudo apt install openjdk-11-jdk
```
Make sure you know the value of your JAVA_HOME environmental variable and export it in your .profile
(or other alternative files).

## Install and configure Tomcat

Apache Tomcat 9 is required to deploy and configure Geocoder.

### Download and install Tomcat
If you haven't installed Tomcat please use the following instructions
to install it. Otherwise, you may skip to the next step. Please note
that if you want to save Geocoder configuration file in a local folder
you need to make sure Tomcat can write to it. You may do this
by add the *ReadWritePaths* value in the SystemD service file as suggested
in [Configuring systemD service](#configuring-systemd-service).


Before installing Tomcat, it's recommended to create a system user for it.
In our example Tomcat will be installed in the /opt/tomcat folder.

```console
$ sudo useradd -m -U -d /opt/tomcat -s /bin/false tomcat
```

Download Tomcat from Apache http://tomcat.apache.org/download-90.cgi. In our example,
we downloaded the version **9.0.35**. Unzip and install the binaries to the destination folder.

```console
$ VERSION=9.0.35
$ wget https://www-eu.apache.org/dist/tomcat/tomcat-9/v${VERSION}/bin/apache-tomcat-${VERSION}.tar.gz -P /tmp
$ sudo tar -xf /tmp/apache-tomcat-${VERSION}.tar.gz -C /opt/tomcat/
```

### Configuring systemD service
Tomcat should be managed as a service by system.
One way to do this is to create a systemD service file.
```console
$ sudo nano /etc/systemd/system/tomcat.service
```

Inside the file, paste the following contents:
```jshelllanguage
[Unit]
Description=Apache Tomcat Web Application Container
After=network.target

[Service]
ReadWritePaths=[A WRITABLE FOLDER PATH]
Type=forking
ProtectSystem=false

Environment=JAVA_HOME=[YOUR JAVA_HOME PATH]
Environment=CATALINA_PID=/opt/tomcat/temp/tomcat.pid
Environment=CATALINA_Home=/opt/tomcat
Environment=CATALINA_BASE=/opt/tomcat
Environment=’CATALINA_OPTS=-Xms512M -Xmx1024M -server -XX:+UseParallelGC’
Environment=’JAVA_OPTS.awt.headless=true -Djava.security.egd=file:/dev/v/urandom’

ExecStart=/opt/tomcat/bin/startup.sh
ExecStop=/opt/tomcat/bin/shutdown.sh

User=tomcat
Group=tomcat
UMask=0007
RestartSec=10
Restart=always

[Install]

WantedBy=multi-user.target
```

Please make sure to replace *YOUR JAVA_HOME PATH*
with the appropriate value. And replace *A WRITABLE FOLDER PATH* with a writable
path if you want to store Geocoder configuration on your local disk.

Now reload system control and enable Tomcat.
```console
$ sudo systemctl daemon-reload
$ sudo systemctl enable --now tomcat
```
You should be able to view Tomcat's default welcome page at http://localhost:8080

You may use the following commands to start/stop Tomcat.

```console
$ sudo systemctl status tomcat
$ sudo systemctl start tomcat
$ sudo systemctl stop tomcat
$ sudo systemctl restart tomcat
```

### Configuring Tomcat admin console

In order to access Tomcat's manager GUI and allow Maven to deploy Geoserver automatically when finish building
Tomcat must have an eligible user account. To do this please modify the tomcat-users.xml file.

```console
$ sudo nano /opt/tomcat/conf/tomcat-users.xml
```

Add a new user and remember its username and password if you don't have one already in the file.
Make sure it has the **manager-gui** and **manager-script** roles.

```xml
<tomcat-users>
  <user username="admin" password="admin" roles="admin-gui,manager-gui,manager-script"/>
</tomcat-users>
```
Here the username and password are both *admin* for demo. 
Please make sure to use a secure password in production.

## Install Apache Maven

You must install and configure Maven properly to deploy Geocoder.
If you don't have Maven installed already please download it
from https://maven.apache.org/download.cgi. Then install it:

```console
$ wget https://mirrors.estointernet.in/apache/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz
$ tar -xvf apache-maven-3.6.3-bin.tar.gz
$ mv apache-maven-3.6.3 /opt/
```

Then append Maven binary path to your .profile and run **source ~/.profile**:
```console
M2_HOME='/opt/apache-maven-3.9.0'
PATH="$JAVA_HOME/bin:$M2_HOME/bin:$PATH"
export PATH
```

You should see Maven is invoked properly.
```console
$ mvn -version
Apache Maven 3.9.0 (9b58d2bad23a66be161c4664ef21ce219c2c8584)
Maven home: /opt/apache-maven-3.9.0
```

### Configuring Maven

In order to allow Maven to deploy Geocoder we need to add/modify it's **settings.xml**
file in the **.m2** folder in your home directory (i.e. **~/.m2/settings.xml**). Please add/modify this file using the following contents:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings
  xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="
    http://maven.apache.org/SETTINGS/1.0.0
    http://maven.apache.org/xsd/settings-1.0.0.xsd
  ">
  <profiles>
    <profile>
      <id>localhost</id>
      <properties>
<tomcatManagerUrl>http://localhost:8080/manager/text</tomcatManagerUrl>
<tomcatManagerUsername>admin</tomcatManagerUsername>
<tomcatManagerPassword>admin</tomcatManagerPassword>
      </properties>
    </profile>
  </profiles>
</settings>
```
Please make sure the username and password (they are both **admin** in this example)
match to the username and password from [Configuring Tomcat admin console](#configuring-tomcat-admin-console).



## Install Geocoder
Geocoder uses a java dependency called **ols-util**.
Ols-util is not yet available from a maven repository, and we must build it first. 
We only need to build it once, then the artifact will be stored in your local **./m2** folder for future uses.

Fetch the ols-util code from GitHub 

```console
$ cd <project_dir>
$ git clone https://github.com/bcgov/ols-util.git
```

Build the code with Maven and add it to the local Maven repository (i.e. **~/.m2**):

```console
$ cd ols-util
$ mvn clean install
```

Now fetch the actual ols-geocoder code from GitHub:

```
$ cd <project_dir>
$ git clone https://github.com/bcgov/ols-geocoder.git
```

Build Geocode with Maven. If you followed all the previous 
instructions, Geocoder will be automatically deployed to Tomcat.

```console
$ cd ols-geocoder
$ mvn clean install -DskipTests -PtomcatDeploy,localhost
```

You have installed Geocoder, but it doesn't work yet because additional configurations are needed.
Before proceeding to the next step please read the following notes:
* The *-DskipTests* option is necessary because Geocoder tests are not presently runnable outside the appropriate
testing environment. This is an area we are planning to improve in the future.

* *-P* enables the named profiles:
  * The "tomcatDeploy" profile connects to Tomcat using the "text" admin interface and uploads and deploys the app.
  * The "localhost" profile is defined locally in **settings.xml** as mentioned in previous steps.
* These profiles may have a different name (id), as long as you refer to that same name in the configure files and commands.
* You may remove the *-PtomcatDeploy,localhost* from the command. It will build the .war files that can be deployed to Tomcat manually.
* If you decide to deploy .war files manually remember to deploy both ols-geocoder-web and ols-geocoder-admin.



## Configuring geocoder

There are essentially tree parts to the configuration:

1. **Bootstrap configuration** using environment variables, which point to the configuration store
2. **Main configuration** in either a directory of files or using an Apache Cassandra database cluster, includes a pointer to the data store
3. **Data store configuration** is a directory of address data files the geocoding is based on.

### Bootstrap Configuration

OLS Geocoder supports storing its configuration as files in a directory, or in tables in a Cassandra keyspace. 
The bootstrap configuration tells the geocoder where to look for the main configuration. 
This is done using the following environment variables:

| Variable | Details |
| -------- | ------- |
| `OLS_GEOCODER_CONFIGURATION_STORE` | may be either `ca.bc.gov.ols.geocoder.config.CassandraGeocoderConfigurationStore` or `ca.bc.gov.ols.geocoder.config.FileGeocoderConfigurationStore` - defaults to: `ca.bc.gov.ols.geocoder.config.CassandraGeocoderConfigurationStore`|
| `OLS_FILE_CONFIGURATION_URL` | if `FileGeocoderConfigurationStore` is used, this must be set to a writeable directory path, to which default configuration files will be written if they are not already present |
| `OLS_CASSANDRA_CONTACT_POINT` | if `CassandraGeocoderConfigurationStore` is used, this specifies the IP name or address of the cassandra contact point - defaults to: `cassandra`|
| `OLS_CASSANDRA_LOCAL_DATACENTER` | if `CassandraGeocoderConfigurationStore` is used, this specifies the deafult datacenter for the cassandra connection - defaults to: `datacenter1`|
| `OLS_CASSANDRA_KEYSPACE` | if `CassandraGeocoderConfigurationStore` is used, this specifies the Cassandra keyspace to use - defaults to `bgeo`|
| `OLS_CASSANDRA_REPL_FACTOR` | if `CassandraGeocoderConfigurationStore` is used, and the Cassandra keyspace is not already created, the keyspace will be created with this replication factor - defaults to "2"|

If the application is being deployed in a docker or kubernetes container, these environment variables can be set in the container definition.
Note that these environment variables need to be set for both the web and admin apps if they are in different containers.

Otherwise, the environment variables should be set inside Tomcat. Create a **setenv.sh** file in the Tomcat folder. 
**Your .profile will not work.**

```console
$ sudo touch /opt/tomcat/bin/setenv.sh
$ sudo nano /opt/tomcat/bin/setenv.sh
```

The following example **setenv.sh** file uses the file configuration mode. The file folder is **/mnt/c/data**. 
(IMPORTANT: the **OLS_FILE_CONFIGURATION_URL** variable expects a URL instead of a path, so it's **file:///mnt/c/data/**):
You can ignore the Cassandra related rows.

```console
export OLS_GEOCODER_CONFIGURATION_STORE="ca.bc.gov.ols.geocoder.config.FileGeocoderConfigurationStore"
export OLS_FILE_CONFIGURATION_URL="file:///mnt/c/data/"
export OLS_CASSANDRA_CONTACT_POINT="localhost"
export OLS_CASSANDRA_LOCAL_DATACENTER="datacenter1"
export OLS_CASSANDRA_KEYSPACE="bgeo"
export OLS_CASSANDRA_REPL_FACTOR="1"
```


After configuring the environment, the Geocoder and Geocoder admin will need to be restarted 
(eg. by restarting tomcat or using the Tomcat manager GUI to restart those particular apps).

Now you should be able to connect to the Geocoder admin app through a path similar to
http://localhost:8080/ols-geocoder-admin-4.4.0-J11/params.jsp. 
The exact address can be found in Tomcat manager GUI at http://localhost:8080/manager/html
(username and password are both *admin* in this example).

### Main configuration
Go to the Geocoder admin application. You should see a list of options.
Click on **Parameter Defaults** to access the main configuration menu.
You MUST set up **dataSource.baseFileUrl** to the folder of the data store files.
Please note this is also a URL so an example value would be **file:///mnt/c/data/**

A detailed explanation of all the configuration options is out of scope for this document.

### Data store configuration

This is the step to set up data store files. In previous step we set the folder URL to **dataSource.baseFileUrl**.
For your convenience a set of sample data files is provided with the code in the **sample-data** directory.
Copy the files to the location you configured, and restart Tomcat or using the Tomcat manager GUI to restart the app.
Geocoder should now be accessible at: http://localhost:8080/pub/geocoder/addresses

```console
$ curl localhost:8080/pub/geocoder/addresses.json?addressString=501%20Belleville%20St.,%20Victoria,%20BC
{"type":"FeatureCollection","queryAddress":"501 Belleville St., Victoria, BC","searchTimestamp":"2023-03-09 16:35:38","executionTime":808.278,"version":"4.3.0","baseDataDate":"","crs":{"type":"EPSG","properties":{"code":4326}},"interpolation":"adaptive","echo":"true","locationDescriptor":"any","setBack":0,"minScore":0,"maxResults":1,"disclaimer":"","privacyStatement":"","copyrightNotice":"Copyright 2015 Province of British Columbia - Open Government License","copyrightLicense":"http:\/\/www.data.gov.bc.ca\/local\/dbc\/docs\/license\/OGL-vbc2.0.pdf","features":[{"type":"Feature","geometry":{"type":"Point","crs":{"type":"EPSG","properties":{"code":4326}},"coordinates":[-123.1213645,49.2582705]},"properties":{"fullAddress":"Vancouver, BC","score":52,"matchPrecision":"LOCALITY","precisionPoints":68,"faults":[{"value":"BELLEVUE ST","element":"STREET_NAME","fault":"notMatched","penalty":12},{"value":"VICTORIA","element":"LOCALITY","fault":"isAlias","penalty":4}],"siteName":"","unitDesignator":"","unitNumber":"","unitNumberSuffix":"","civicNumber":"","civicNumberSuffix":"","streetName":"","streetType":"","isStreetTypePrefix":"","streetDirection":"","isStreetDirectionPrefix":"","streetQualifier":"","localityName":"Vancouver","localityType":"City","electoralArea":"","provinceCode":"BC","locationPositionalAccuracy":"coarse","locationDescriptor":"localityPoint","siteID":"","blockID":"","fullSiteDescriptor":"","accessNotes":"","siteStatus":"","siteRetireDate":"","changeDate":"","isOfficial":"true"}}]}
```


The Geocoder service only provides a REST API, you may want to consider installing the OLS-DevKit to get a web UI with a map, etc.

General purpose software for creating the Geocoder data files is not yet available. 
To run the Geocoder using different data, translations would need to be developed following the example of the sample data.
The code in the geocoder-process project performs some necessary transformations for the province of British Columbia, 
and may be of some use. A future goal is to develop flexible data transformation tools for this purpose.


## Troubleshooting

```console
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-javadoc-plugin:3.2.0:jar (attach-javadoc) on project opentelemetry-common:
MavenReportException: Error while generating Javadoc: Unable to find javadoc command: The environment variable JAVA_HOME is not correctly set.
```
This is a known issued with the javadoc plugin. Please upgrade it to 3.3.1 version or up (by editing the pom.xml file).

```console
500... when access Geocoder services...
```
It's most likely that there is a data store configuration error.
Please read the error message in the stack as it may give you some hints:
* Make sure the file URLs in the configuration files are URLs (not a path)
* All data store files must be present. Use the example files to find if you miss any.
* The data store files must not have any syntax error. 
* The street names in the data store files must not be blank.





 
