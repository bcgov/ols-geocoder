# Open Location Service Geocoder Service Installation

## Overview

The OLS Geocoder service is a Java 11 web application which runs on the Tomcat 9 Java application server.
The application is built using Maven, deployed to Tomcat, and configured. 

Pre-requisites to build and installation include:
- Java 11 (OpenJDK 11 with HotSpot JVM recommended)
- Apache Tomcat 9.0+
- Apache Maven 3.6+
- Optionally, Apache Cassandra database for configuration

There are essentially 3 parts to the configuration:

1. initial bootstrap configuration using environment variables, which point to the configuration store
2. main configuration in either a directory of files or using an Apache Cassandra database cluster, includes a pointer to the data store
3. data store is a directory of data files on which the geocoding is based

## Build and Deploy the Application

Fetch the ols-util code from GitHub (ols-util is a library of code shared by OLS Geocoder and Router which is not yet available from a maven repository):

```
cd <project_dir>
git clone https://github.com/bcgov/ols-util.git
```

Build the code with Maven and install into the local Maven repository:

```
cd ols-util
mvn clean install
```

Fetch the ols-geocoder code from GitHub:

```
cd <project_dir>
git clone https://github.com/bcgov/ols-geocoder.git
```

Build the code with Maven:

```
cd ols-geocoder
mvn clean install -DskipTests -PtomcatDeploy,localhost
```

The -DskipTests option is necessary because the geocoder tests are not presently runnable outside of the appropriate testing environment. 
This is an area we are planning to improve in the future.

-P enables the named profiles, which in maven are just chunks of configuration. 
The "tomcatDeploy" profile connects to tomcat using the "text" admin interface and uploads and deploys the app. 
The "localhost" profile is defined locally in a file called settings.xml in the ".m2" folder in the user's home directory, 
eg. c:/users/<yourusername>/.m2/settings.xml on windows. Here are the relevant parts of the settings.xml:

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
<tomcatManagerUsername>tomcat</tomcatManagerUsername>
<tomcatManagerPassword>tomcat</tomcatManagerPassword>
      </properties>
    </profile>
  </profiles>
</settings>
```

The profile can have a different name (id) as appropriate, as long as you refer to that same name in the list of profiles in the mvn build command. 
The URL, username, and password must match up with your Tomcat configuration. A user must be configured in the tomcat-users.xml file to have the "manager-script" role and 
put the associated username and password into your maven settings.xml file, along with the correct URL. 

Alternatively, you can remove the -PtomcatDeploy,localhost from the maven command line, and just build the .war files and deploy them to tomcat 
manually using the Tomcat manager web UI. Note that there are two separate web applications that need to be deployed: ols-geocoder-web and ols-geocoder-admin.

## Bootstrap Configuration

OLS Geocoder supports storing its configuration as files in a directory, or in tables in a Cassandra keyspace. The bootstrap configuration tells the geocoder where to look 
for the main configuration. This is done using the following environment variables:

- OLS_GEOCODER_CONFIGURATION_STORE - may be either "ca.bc.gov.ols.geocoder.config.CassandraGeocoderConfigurationStore" or "ca.bc.gov.ols.geocoder.config.FileGeocoderConfigurationStore" - defaults to: "ca.bc.gov.ols.geocoder.config.CassandraGeocoderConfigurationStore"
- OLS_FILE_CONFIGURATION_URL - if FileGeocoderConfigurationStore is used, this must be set to a writeable directory path, to which default configuration files will be written if they are not already present 
- OLS_CASSANDRA_CONTACT_POINT - if CassandraGeocoderConfigurationStore is used, this specifies the IP name or address of the cassandra contact point - defaults to: "cassandra"
- OLS_CASSANDRA_LOCAL_DATACENTER - if CassandraGeocoderConfigurationStore is used, this specifies the deafult datacenter for the cassandra connection - defaults to: "datacenter1"
- OLS_CASSANDRA_KEYSPACE - if CassandraGeocoderConfigurationStore is used, this specifies the Cassandra keyspace to use - defaults to "bgeo"
- OLS_CASSANDRA_REPL_FACTOR - if CassandraGeocoderConfigurationStore is used, and the Cassandra keyspace is not already created, the keyspace will be created with this replication factor - defaults to "2"

If the application is being deployed in a docker or kubernetes container, these environment variables can be set in the container definition. 
Otherwise, use the appropriate tools for your operating system to set the variables; the Tomcat catalina startup script might be a good place to set them.
Note that these environment variables need to be set for both the web and admin apps if they are in different containers. 

After configuring the environment, the geocoder and admin application will need to be restarted, eg. by restarting tomcat or using the tomcat manager GUI to restart those particular apps. 

## Data Configuration

Once the boostrap configuration is in place, the admin app should allow you to edit the main configuration. A detailed explanation of all the configuration options is out of scope for this document;
however there is one key configuration parameter that must be changed: **dataSource.baseFileUrl**, which has a default value of "file:///C:/apps/bgeo/data/". This must be set to a URL accessible to 
the application which contains the geocoder data. A set of sample data is provided with the code in the sample-data directory. This data needs to be placed somewhere accessible to the application,
either using at http/https: URL or on the filesystem using a file: URL. Once the data files are in place and the admin app has been used to set the value of the dataSource.baseFileUrl configuration 
parameter correctly, the geocoder web app must be restarted, eg. by restarting tomcat or using the tomcat manager GUI to restart those particular apps.

At this point you should have a working geocoder. The geocoder service only provides a REST API, you may want to consider installing the OLS-DevKit to get a web UI with a map, etc. 

General purpose software for creating the Geocoder data files is not yet available. To run the Geocoder using different data, translations would need to be developed following the example of the sample data. 
The code in the geocoder-process project performs some of the necessary transformations for the province of British Columbia, and may be of some use. 
A future goal is to develop flexible data transformation tools for this purpose. 

 