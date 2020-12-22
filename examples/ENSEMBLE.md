# Simple Ensemble Classifier

## Overview

In this example, we will build a simple ensemble classifier to solve a binary prediction problem.
We will use the Iris Flower dataset and predict if a given record is a setosa flower or not.

### Learning Goals

In the following example, we will show:

* How to define a pipeline with multiple stages.

* How to define protobuf interfaces for the stages.

* How to send the same message to multiple services.

* How to merge multiple outputs into a single message.

We will begin by defining the necessary [protobuf](https://developers.google.com/protocol-buffers/docs/proto3) interfaces for the stages.
Then, we will specify the configuration for the orchestrator to link the stages together.

### Example Architecture

The system has 5 stages:

 * ```Source``` stage that will provide the data records for the classification.

 * ```Logistic Regression``` stage that classifies the received data using a logistic regression.

 * ```Decision Tree``` stage that classifies the received data using a decision tree.

 * ```SVM``` stage that classifies the received data using an SVM.

 * ```Sink``` stage that will perform a majority voting among the received classifications and store the classification.

We will assume the models were all previously trained.

```
                  |---------------------|
                / | Logistic Regression | \
               /  |---------------------|  \
              /                             \
|----------| /    |---------------------|    \ |----------|
|  Source  | ---- |    Decision Tree    | ---- |   Sink   |
|----------| \    |---------------------|    / |----------|
              \                             /
               \  |---------------------|  /
                \ |         SVM         | /
                  |---------------------|
```

We this architecture, the source sends the same message *(the record)* to all the classifiers.
Also, the sink will receive one message which is the merge of all the outputs for the classifiers.

## Protobuf Interfaces

### Source Stage

The source stage will provide the data to the ensemble.
It will receive a message from the orchestrator to request for a record and will reply with the next record to classify.
In our case, we will send four features: sepal and petal width and length, just as in the iris dataset.

```proto
syntax = "proto3";

service Source {
    // Method for Source Stage
    rpc GetRecord(Empty) returns (Record);
}

// This stage input is empty since 
// it just serves to get another record
message Empty {}

message Record {
    double sepal_length = 1;
    double sepal_width = 2;
    double petal_length = 3;
    double petal_width = 4;
}
```

### Classifiers Stages

Now we will define the classifiers. 
They will all have a very similar structure as their purpose is equal.

Notice they all receive the same `Record` message defined in the source, 
which is vital since they will all receive the same message from the `Source` stage.

Also, they all output the same `IndividualClassification` message, but we could have different outputs as the messages will be merged for the `Sink` stage.

Here, we joined all the services together, but they can also be defined in separate files.

```proto
syntax = "proto3";

service LogisticRegression {
    // Method for Logistic Regression Stage
    rpc Classify(Record) returns (IndividualClassification);
}

service DecisionTree {
    // Method for Decision Tree Stage
    rpc Classify(Record) returns (IndividualClassification);
}

service SVM {
    // Method for SVM Stage
    rpc Classify(Record) returns (IndividualClassification);
}

// This message is equal to the one returned by the source stage
message Record {
    double sepal_length = 1;
    double sepal_width = 2;
    double petal_length = 3;
    double petal_width = 4;
}

message IndividualClassification {
    bool is_setosa = 1;
}
```

### Sink Stage

Finally, we define the sink stage. 
This stage will receive the predictions from the classifiers, perform a majority voting and store the classification result.

Notice that the `ClassificationResult` message combines the `IndividualClassification` message from all the classifiers stages.

```proto
syntax = "proto3";

service Sink {
    // Method for Sink Stage
    rpc Classify(ClassificationResult) returns (Empty);
}

// This message will aggregate all the results from the classifiers
message ClassificationResult {
    IndividualClassification lr_result = 1;
    IndividualClassification dt_result = 2;
    IndividualClassification svm_result = 3;
}

// This message is equal to the one returned by the individual classifiers
message IndividualClassification {
    bool is_setosa = 1;
}

// This state output is empty since 
// all the work should be executed within the method
// and no information must be sent to any other stage.
// The contents of this message will be ignored by the orchestrator.
message Empty {}
```

## Orchestrator Configuration File

Now we can define the configuration file that the orchestrator will use to link the stages together.

The file is split into two sections: the stages and the links. 
In the first, we will define the 5 stages.
In the second, we will define the links. 
This is when we assure that the output of the `Source` stage is duplicated to the classifiers,
and that the predictions are merged for the `Sink` stage.

### Stages

We will define arbitrary hosts and ports for the stages. 
We assume the stages are all executed in different containers that only offer the specified single service,
so the name of the method is irrelevant.

```yaml
stages:
  - name: "Source"
    host: host-1
    port: 10001
    method:
      type: UNARY
  - name: "Logistic Regression"
    host: host-2
    port: 10002
    method:
      type: UNARY
  - name: "Decision Tree"
    host: host-3
    port: 10003
    method:
      type: UNARY
  - name: "SVM"
    host: host-4
    port: 10004
    method:
      type: UNARY
  - name: "Sink"
    host: host-5
    port: 10005
    method:
      type: UNARY
```

### Duplicate Source Messages

Now we will address how to duplicate the messages from the `Source` to all the classifiers.
We can do this by creating a link that sends the whole output from `Source` to each classifier.

```yaml
links:
  - source:
      stage: "Source"
    target:
      stage: "Logistic Regression"
  - source:
      stage: "Source"
    target:
      stage: "Decision Tree" 
  - source:
      stage: "Source"
    target:
      stage: "SVM"
```

### Merge classification results

In order to merge the results for the `Sink` stage,
we need to define the field that will set with each classifier output.

```yaml
links:
  - source:
      stage: "Logistic Regression"
    target:
      stage: "Sink"
      # The message from Logistic Regression
      # will be used in the field lr_results
      # for the ClassificationResult message
      field: lr_result
  - source:
      stage: "Decision Tree"
    target:
      stage: "Sink"
      field: dt_result
  - source:
      stage: "SVM"
    target:
      stage: "Sink"
      field: svm_result
```

### Complete File

We finish with specification of the entire configuration file:

```yaml
stages:
  - name: "Source"
    host: host-1
    port: 10001
    method:
      type: UNARY
  - name: "Logistic Regression"
    host: host-2
    port: 10002
    method:
      type: UNARY
  - name: "Decision Tree"
    host: host-3
    port: 10003
    method:
      type: UNARY
  - name: "SVM"
    host: host-4
    port: 10004
    method:
      type: UNARY
  - name: "Sink"
    host: host-5
    port: 10005
    method:
      type: UNARY
links:
  - source:
      stage: "Source"
    target:
      stage: "Logistic Regression"
  - source:
      stage: "Source"
    target:
      stage: "Decision Tree"
  - source:
      stage: "Source"
    target:
      stage: "SVM"
  - source:
      stage: "Logistic Regression"
    target:
      stage: "Sink"
      field: lr_result
  - source:
      stage: "Decision Tree"
    target:
      stage: "Sink"
      field: dt_result
  - source:
      stage: "SVM"
    target:
      stage: "Sink"
      field: svm_result
```
