# GrpcOrchestrator

## Project Overview

GrpcOrchestrator is an orchestrator for Grpc Services. 
It is able to automatically connect arbitrary reflection enabled Grpc Services.

It delivers outputs from one service to the next.
It is also capable of merging messages from multiple services and passing them to the next
one as well as splitting an output from one service into sub-messages and send them to different services.

## Usage

This app can be deployed using a docker container.
The configuration for the gRPC services is defined in a configuration file.

In order to deploy the app with docker execute the following steps:

* Build the base docker image *(This image is the same for all pipelines and has everything except the pipeline configuration)*:

```
$ gradle buildDocker
```

* Create a configuration file for the services according to [the specification](CONFIGURATION.md)

* Create a new docker image from the base image with the configuration file *(The Dockerfile can be something like)*:

```
# Use base image
FROM orchestrator:latest

# Copy configurations
COPY <config_file_path> .

# Set the CONFIG_FILE environment variable to 
# tell the application the location of the configuration file
ENV CONFIG_FILE=<config_file_name>
```
