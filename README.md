# GrpcOrchestrator

## Project Overview

GrpcOrchestrator is an orchestrator for Grpc Services. 

It delivers outputs from one service to the next.
It is also capable of merging messages from multiple services and passing them to the next
one as well as splitting an output from one service into sub-messages and send them to different services.

## Usage

This app can be deployed using a docker container or manually run with gradle.
The pipeline configuration for the orchestrator is defined in a [configuration file](CONFIGURATION.md).

### Docker

When running the orchestrator with Docker, the environment variable `CONFIG_FILE` must be set to the path of the configuration file.

In order to deploy the app execute the following steps:

* Pull the orchestrator image from DockerHub

```shell
$ docker pull sipgisr/grpc-orchestrator:latest
```

* Run the image and add the following flags to set up the configuration file:

    * Mount the config file in /app/\<config_file_name\>:

    ```shell
    --mount type=bind,source=<config_file_path>/<config_file_name>,target=/app/<config_file_name>
    ```

    * Set `CONFIG_FILE` to \<config_file_name\>:

    ```shell
    --env CONFIG_FILE=<config_file_name>
    ```

### Gradle

To execute the app with gradle we just need to clone this repository and run the respective gradle task:

* On Linux or macOS just execute:

```shell
$ ./gradlew run -DconfigFile="<config_file_path>"
```

* On Windows run *(Not tested but should work)*:

```shell
$ gradlew.bat run -DconfigFile="<config_file_path>"
```

## Building Pipelines

In this section, we give some general guidelines on how to develop pipelines with GrpcOrchestrator.

### Service Interfaces

The Orchestrator only connects the gRPC services, according to the specified configuration.
It takes the output of some service and calls the method in the next stage with the given output.

As such, no extra verifications are made to see if both the type of the message 
that is received, and the expected type for the method in the next stage are equal.

### gRPC Reflection

In order to discover the method that should be executed at each stage, 
the GrpcOrchestrator uses gRPC reflection, so you need to enable it in your services in order to create the pipeline.

You can see how to enable reflection in specific languages 
[here](https://github.com/grpc/grpc/blob/master/doc/server-reflection.md).

## Configuration Examples

We provide several examples of configuration files to achieve different configurations.
We define the protobuf interfaces of hypothetical services and provide a configuration file for the orchestrator.
This configuration file would configure the orchestrator to transfer the messages between the services.

Each example focuses on a different specificity of architecture:

* [Simple Ensemble Classifier](examples/ENSEMBLE.md) *(Duplicate and Merge Messages)*