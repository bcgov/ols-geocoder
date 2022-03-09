## Helm Chart to Generate Geocoder Build/Deploy Pipeline

## Running

Checkout https://github.com/bcgov-dss/loc-tools/tree/main/helm/tekton-pipelines

From tekton-pipelines/ run (where geocoder-helm-1s is a chart name)

```
helm install geocoder-helm-1s . --values=examples/geocoder-pr-trigger.yaml
```

To uninstall:

```
helm uninstall geocoder-helm-1s
```

## Prerequisites:

### GitHub deploy token

- Generate ssh key:

```
ssh-keygen -C "geocoder-build-and-deploy/repo@github" -f geocoder-github -N ''
```

- Set public ssh key as deploy key in GitHub

- Create ssh secret:

```
oc create secret generic git-creds
--from-file=ssh-privatekey=./geocoder-github
--type=kubernetes.io/ssh-auth
```

- Add tekton annotation for github to secret
```
oc annotate secret git-creds "tekton.dev/git-0=github.com"
```

- Create service account that uses git-creds secret:

```
oc create -f git-service-accnt.yaml
```

### GitHub WebHooks
- Create WebHook secret in the following format:

```
kind: Secret
apiVersion: v1
metadata:
  name: github-secret
type: Opaque
data:
  secretToken: dG9rZW4=
```

### GitHub API
- Using GitHub API requires a GitHub account (aka machine or service account), it is not enough to use existing repo token and webhook

- Personal access token for the machine account needs to be created:

```
apiVersion: v1
kind: Secret
metadata:
  name: github-auth-token
type: Opaque
data:
  token: dG9rZW4=
```

### Network Policy
- make sure network policy is set to allow external traffic - need this to expose event listener route

### Storage
- Create shared access PVC to cache .m2 repo
