# Caddy, MinIO with shared PVC

The intention is that MinIO provides the ability to add,remove,udated files on the shared PVC.
Caddy is used to provide http access to the files.


# Provisioning example

```bash
echo "Provisioning gc-address-range-gen-datastore (Address Range Generator)"
oc \
    -n ${YOUR_NAMESPACER} \
    process \
    -p ROUTE_SUBDOMAIN=apps.silver.devops.gov.bc.ca \
    -p APPLICATION_NAME=gc-address-range-gen-datastore \
    -f caddy-minio-pvc-template.yaml \
    -o yaml \
    | oc apply \
      -n ${YOUR_NAMESPACER} -f -
```
