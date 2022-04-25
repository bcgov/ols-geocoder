
# Geocoder API - Build and OpenShift Deployment

Physical Address Geocoder

## Build Instructions

The Following Jenkins Job is used to Build the artifacts:

 [OLS Geocoder Build](https://cis.apps.gov.bc.ca/int/view/LOC/job/ols/job/OLS%20OSS%20Jobs/job/OLS%20Geocoder%20Build/)

Artifacts are managed in Artifactory:

https://delivery.apps.gov.bc.ca/artifactory/

`lib-snapshot-repo` and   `lib-release-repo` folders.


## OpenShift Deployment Instructions

1. Setup image pull secrets
2. Optional - setup Service Accounts for remote automation
3. Provision Data Providers
4. Create BuildConfigs in tools namespace for geocoder-web and admin app.
5. Provision the Geocoder in destination namespace
6. Post provision steps
   a. Copy relevant data into data Providers
   b. Update config with relevant configurations.
7. Configure or update Kong Gateway geocoders

## Templates

## `caddy-minio-pvc-template.yaml`

This Template provisions a Caddy server and a MinIO sever that share the same PVC.
* Minio is used to transport/upate the data files.
* Caddy is used to server the data files to the Geocoder API nodes - when the pod startes it reads source data files from the caddy server.

### Example
```bash
#!/bin/bash

namespace=988040-dev
tools_namespace=988040-tools

echo "Provisioning Geocoder Datastore DLV"
oc \
    -n ${namespace} \
    process \
    -p ROUTE_SUBDOMAIN=apps.silver.devops.gov.bc.ca \
    -p APPLICATION_NAME=geocoder-datastore-dlv \
    -p TOOLS_NAMESPACE=${tools_namespace} \
    -f caddy-minio-pvc-template.yaml \
    -o yaml \
    | oc apply \
      -n ${namespace} -f - # --dry-run=client
```

## `geocoder-web-bc.yaml`

This contains the build config for:  
`ols-geocoder-sidecar`

This file provides a BuildConfig object for the geocoder container.
The resulting BuildConfig take ols-geocoder-web WAR file as input and when run, creates an imageStream for the geocoder app.

### Example  
```bash
oc create -f geocoder-admin-bc.yaml -o yam -n 988040-tools
```
#### Remove
```bash
# use get first to check
oc get all -l app=geocoder-sidecar -n 988040-tools \
 | oc apply -f - -n 988040-tools
...
# use delete
oc delete all -l app=geocoder-sidecar -n 988040-tools
```

## `geocoder-admin-bc.yaml`

This contains the build config for:  
`ols-geocoderr-admin-sidecar`

This file provides a BuildConfig object for the `ols-geocoder-admin` container.
The resulting BuildConfig take `ols-geocoder-admin` WAR file as input and when run, creates an imageStream for the ols-geocoderr-admin app.

### Example  
```bash
oc process -f geocoder-admin.bc.yaml -o yaml \
 | oc apply -f - -n 988040-tools
```
#### Remove
```bash
# use get first to check
oc get all -l app=geocoder-admin-sidecar -n 988040-tools
...
# use delete
oc delete all -l app=geocoder-admin-sidecar -n 988040-tools
```

## `geocoder-template.yaml`

This provision all the objects relevant to the Geocoder API.  This includes

* Geocoder API web app
* Data (or) Config Admin Web App
* Cassandra Cluster
* necessary services and geocoders
* necessary NetworPolcies.

### Example

```bash
#!/bin/bash

APP_NAME=geocoder
ENV=dev
GEOCODER_IS_TAG=latest
DATA_ADMIN_IS_TAG=latest

# oc get all -l app=ols-geocoder-web
# oc delete  all -l app=ols-geocoder-web

oc process -f geocoder-template.yaml \
    -p APP_NAME=${APP_NAME} \
    -p ENV=${ENV} \
    -p GEOCODER_IS_TAG=${GEOCODER_IS_TAG} \
    -p DATA_ADMIN_IS_TAG=${DATA_ADMIN_IS_TAG} \
    -o yaml \
    | oc apply -f - -n 988040-prod #\
    #--dry-run=client
    #| yq -C - r
```
#### Alternatively  
Copy or rename `example.env` to `dev.env` for this example to work. _make changes as required
```bash
$ cat dev.env
APP_NAME=geocoder
ENV=dev
GEOCODER_IS_TAG=latest
DATA_ADMIN_IS_TAG=latest
$
$
$ oc process -f geocoder-template.yaml --param-file=dev.env -o yaml | oc apply -f - -n 988040-prod

```

#### Remove

```bash
# use get first to check
oc get all -n 988040-dev -l app=geocoder-template
# delete
oc delete all -n 988040-dev -l app=geocoder-template
```
