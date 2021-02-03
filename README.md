# GrpcOrchestrator

## Project Overview

GrpcOrchestrator is an orchestrator for Grpc Services. 
It is able to automatically connect arbitrary reflection enabled Grpc Services.

It delivers outputs from one service to the next.
It is also capable of merging messages from multiple services and passing them to the next
one as well as splitting an output from one service into sub-messages and send them to different services.

## Usage

This app can be deployed using a docker container or manually run with gradle.
The configuration for the orchestrator is defined in a [configuration file](CONFIGURATION.md).

### Docker

When running the orchestrator with Docker, the environment variable `CONFIG_FILE` must be set to the path of the configuration file.

In order to deploy the app execute the following steps:

* Build the base docker image *(This image is the same for all pipelines and has everything except the pipeline configuration)*:

```shell
$ ./gradlew buildDocker
```

* Choose one of the following alternatives to specify the pipeline configuration:
  
#### Mount the configuration

* Mount the config file in /app/\<config_file_name\> by running the container with the following flag:

```shell
--mount type=bind,source=<config_file_path>/<config_file_name>,target=/app/<config_file_name>
```

* Set `CONFIG_FILE` to \<config file path\> by running the container with the following flag:

```shell
--env CONFIG_FILE=<config_file_name>
```

#### Create a new image with the configuration

* Create a new docker image from the base image with the configuration file *(The Dockerfile can be something like)*:

```docker
# Use base image
FROM orchestrator:latest

# Copy configurations
COPY <config_file_path> .

# Set the CONFIG_FILE environment variable to 
# tell the application the location of the configuration file
ENV CONFIG_FILE=<config_file_name>
```

* Build and run the new image.

### Gradle

To execute the app with gradle we just need to run the respective gradle task:

* On Linux or macOS just execute:

```shell
$ ./gradlew run -DconfigFile="<config_file_path>"
```

* On Windows run *(Not tested but should work)*:

```shell
$ gradlew.bat run -DconfigFile="<config_file_path>"
```

## Configuration Examples

We provide several examples of configuration files to achieve different configurations.
We define the protobuf interfaces of hypothetical services and provide a configuration file for the orchestrator.
This configuration file would configure the orchestrator to transfer the messages between the services.

Each example focuses on a different specificity of architecture:

* [Simple Ensemble Classifier](examples/ENSEMBLE.md) *(Duplicate and Merge Messages)*