# Configuration File Reference

## Overview

The configuration file is a [yaml](https://yaml.org) file with 2 mandatory sections: [stages](#stages-section) and [links](#links-section).
The first section defines the stages for the pipeline. 
The second defines the connections between the stages.

## Stages Section

A stage is a method in a gRPC server that can be executed. It has a predefined input and output according to a protobuf service definition.

The configuration file must declare a ```stages``` section. This section defines a list of stages. Each element defines a stage of the pipeline that will be queried by the orchestrator to.

Each stage must have a name, a host and a port. Also, it must define the gRPC method to be executed. The method may have a name and must have a type.

### name

```name``` sets the name of the stage. 
Will serve as an identifier for the stage when defining the links between stages.

### host

```host``` specifies the host where the gRPC server with the stage is running.

### port

```port``` specifies the port where the gRPC server with the stage is running.

### method

```method``` specifies the name for the grpc method to execute. May be omitted if the server only has one method.

### Example

An example of a definition of two stages would be:

```yaml
stages:
  - name: "Stage 1"
    host: host-1
    port: 10001
    method: Method1
  - name: "Stage 2"
    host: host-2
    port: 10002
```

## Links Section

This section defines a list of connections between the stages. This will determine how the data will flow through the pipeline. Each connection must define a source and a target.

### Source

```source``` specifies the stage that sends messages in the link. 
It has the following variables:

* ```stage``` is the name of the source stage for the connection.

* ```field``` is not mandatory. 
  If not defined, then the whole the message will be sent to the target stage. 
  Otherwise, it defines the field name of the source output name that will be transferred through the connection. 

### Target

```target``` specifies the stage that receives messages in the link. 
It has the following variables:

* ```stage``` is the name of the target stage for the connection.

* ```field``` is not mandatory. 
  If not defined, then the whole received message is delivered to the stage. 
  Otherwise, the default message for the target stage is created, and the field with the given variable name is set with the received message.

### Example

An example of a link between the two above stages would be:

```yaml
links:
  - source:
      stage: "Stage 1"
      field: Field1
    target:
      stage: "Stage 2"
      field: Field2
```
