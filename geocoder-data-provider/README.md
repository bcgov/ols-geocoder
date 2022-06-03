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
    -p TOOLS_NAMESPACE=${TOOLS_NAMESPACE} \
    -f caddy-minio-pvc-template.yaml \
    -o yaml \
    | oc apply \
      -n ${YOUR_NAMESPACER} -f -
```

# Setting up bucket webhooks

```
// create a bucket
mc mb data/geocoder;
// create a webhook, where $WEBHOOK_URL_GEOCODER is a target endpoint
mc admin config set data-integration-minio notify_webhook:geocoder queue_limit='10' endpoint=$WEBHOOK_URL_GEOCODER queue_dir='/.mc/queue/';
// restart service
mc admin service restart data-integration-minio;
// assign webhook to bucket 
mc event add data-integration-minio/geocoder arn:minio:sqs::geocoder:webhook --event put --suffix .files --prefix geocoder;
```
