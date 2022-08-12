
# Geocoder API - Build and OpenShift Deployment

Physical Address Geocoder

## Build Instructions

Helm Chart instructions to provision CI/CD Tekton pipeline:
https://github.com/bcgov-dss/loc-tools/tree/main/helm#988040-tools

Artifacts are managed in Artifactory:
https://artifacts.developer.gov.bc.ca/artifactory/g988-libs/


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
oc create -f geocoder-web-bc.yaml -o yam -n 988040-tools \
 | oc apply -f - -n 988040-tools
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
oc apply -f geocoder-admin-bc.yaml
```
#### Remove
```bash
# use get first to check
oc get all -l app=geocoder-admin-sidecar -n 988040-tools
...
# use delete
oc delete all -l app=geocoder-admin-sidecar -n 988040-tools
```

## `cfg-maps.yaml`

Geocoder config files.

```
oc process -f cfg-maps.yaml | oc apply -f -
```

## `geocoder-template.yaml`

This provision all the objects relevant to the Geocoder API.  This includes

* Geocoder API web app
* Data (or) Config Admin Web App
* necessary services and geocoders
* necessary NetworPolcies.

## Deploy

### Dev

```bash
$ oc process -f geocoder-template.yaml --param-file=env.dev -o yaml | oc apply -f - -n 988040-prod

```
### Test

```bash
$ oc process -f geocoder-template.yaml --param-file=env.test -o yaml | oc apply -f - -n 988040-prod

```

### Data Integration

```bash
oc process -f geocoder-template.yaml --param-file=env.data -o yaml | oc apply -f - -n 988040-prod
```

## Remove

### Example

```bash
# use get first to check
oc get all -n 988040-dev -l app=geocoder-template
# delete
oc delete all -n 988040-dev -l app=geocoder-template
```

# Batch Address Range Generator (aka BARG)

* This is a Tekton pipeline that is run during monthly data refresh
  * The Job then runs the artifact to generate the addresses.
  * It uses ols-geocoder-process module from ols-geocoder project. This in turn has a dependancy on `ols-geocoder-core`
  * it is used to generate address ranges: this is a step in the monthly data refresh.
* Details of the pipeline can be found in https://github.com/bcgov-dss/loc-tools/blob/main/helm/tekton-pipelines/examples/geocoder-data-integration-triggers.yaml under `minio-baarg-update`
* Data integration pipelines can be deployed/torn down via steps described at https://github.com/bcgov-dss/loc-tools/tree/main/helm under `geocoder-data-integration-triggers.yaml`
* The instructions for triggering the pipeline can be found at https://github.com/bcgov-dss/loc-tools/tree/main/helm under `minio-baarg-update`
