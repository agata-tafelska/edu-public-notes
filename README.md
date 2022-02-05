# edu-public-notes

Simple educational project to share notes publicly

This project is responsible for providing notes API.
See [Open API definition](service-notes/api/notesApi.yaml) to get more details.

## Requirements
* JDK 11
* Docker

## Build and run locally

### Infrastructure

Service requires database which can be provided with `docker compose` by running following tasks:

```shell
./gradlew service-notes:runLocalInfrastructure 
```

In case fresh infrastructure state is needed, current one can be cleaned by:

```shell
./gradlew service-notes:cleanLocalInfrastructure
```

### Running service

To simply run the service execute:

```shell
./gradlew service-notes:bootRun
```
## Build and run on Azure

Project contains Terraform configuration allowing simple provisioning infrastructure to deploy
it on Azure.

Following cloud services are used:
* Azure Container Registry (to store application's Docker image)
* PostgreSQL Server (main database used for the service)
* App Service (service allowing to run Docker containers)

### Requirements

* Azure account
* Azure CLI installed and signed into account
* Terraform CLI installed

### Steps to deploy to Azure

1. Provision infrastructure

```shell
cd infrastructure
terraform apply
```

Enter DB admin username and password when asked.

2. Build and push Docker container to Azure Container Registry

Find Container Registry username (`admin_username`) and password (`admin_password`) from Terraform state:

```shell
cd infrastructure
terraform show -json
```

Build and publish container

```shell
./gradlew clean service-notes:bootJar service-notes:buildDockerImage
docker login edupublicnotescontainerregistry.azurecr.io
docker tag service-notes edupublicnotescontainerregistry.azurecr.io/service-notes
docker push edupublicnotescontainerregistry.azurecr.io/service-notes
```

### Test if app is running

After provisioning infrastructure and pushing container (may take few minutes) service will start
automatically.

To verify if it's working simply check main functionalities:

1. Getting notes via HTTP GET request

```shell
curl https://edu-public-notes-service.azurewebsites.net/notes
```

Expected response:
```json
{"notes":[]}
```

2. Adding note via HTTP PUT request

```shell
curl -X PUT -H "Content-Type: application/json" -d '{"text":"Example note"}' "https://edu-public-notes-service.azurewebsites.net/notes"
```

Added note should be returned in the response 
