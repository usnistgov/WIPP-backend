# WIPP REST API
A Java Spring Boot application for managing WIPP data and workflows. 
The API follows the HATEOAS architecture using the HAL format.

## Requirements
Requirements for development environment setup.

### Java environment
* Java JDK 8 (1.8)
* Maven version compatible with Java 8

### Database
* MongoDB 3.6

### Kubernetes cluster
* For development purposes, a single-node cluster can be easily installed using [Minikube](https://github.com/kubernetes/minikube) or [Docker for Mac on macOS](https://docs.docker.com/docker-for-mac/#kubernetes)
* We are using [Argo workflows](https://argoproj.github.io/argo/) to manage workflows on a Kubernetes cluster, installation instructions for version 2.2.1 can be found [here](https://github.com/argoproj/argo/blob/release-2.2/demo.md)

### Data storage
* Create a `WIPP-plugins` folder in your home directory for data storage (`dev` Maven profile is expecting the data folder location to be `$HOME/WIPP-plugins`)
* Create the WIPP data storage Persistent Volume (PV) and Persistent Volume Claim (PVC) in your Kubernetes cluster following the templates for hostPath PV and PVC available in [WIPP-deploy](https://github.com/usnistgov/WIPP-deploy/tree/develop/deployment/volumes)
    * `path` of `hostPath` in `hostPath-wippdata-volume.yaml` should be modified to match path of `WIPP-plugins` folder created above
    * `storage` of `capacity` is set to 100Gi by default, this value can be modified in `hostPath-wippdata-volume.yaml` and `hostPath-wippdata-pvc.yaml`
    * run `hostPath-deploy.sh` to setup the WIPP data PV and PVC

## Compiling
```shell
mvn clean install
```
## Running
```shell
cd wipp-backend-application
mvn spring-boot:run
```
The WIPP REST API will be launched with the `dev` profile and available at `http://localhost:8080/api`

## Docker packaging
The Maven `prod` profile should be used for Docker packaging, even for testing/development purposes:
```sh
mvn clean package -P prod
docker build --no-cache . -t wipp_backend
```
For a Docker deployment of WIPP on a Kubernetes cluster, scripts and configuration files are available in the [WIPP-deploy repo](https://github.com/usnistgov/WIPP-deploy/tree/develop/deployment).

## WIPP Development flow
We are following the [Gitflow branching model](https://nvie.com/posts/a-successful-git-branching-model/) for the WIPP development.  
To accommodate the specificities of the Maven version management, we are using the [JGitFlow plugin](https://bitbucket.org/atlassian/jgit-flow/wiki/Home).

### Contributing
Please follow the [Contributing guidelines](CONTRIBUTING.md)