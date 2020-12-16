# GrpcOrchestrator

## Project Overview

GrpcOrchestrator is an orchestrator for Grpc Services. 
It is able to automatically connect arbitrary reflection enabled Grpc Services.
It delivers outputs from one service to the next.
It is also capable of merging messages from multiple services and passing them to the next
one as well as splitting an output from one service into sub-messages and send them to different services.