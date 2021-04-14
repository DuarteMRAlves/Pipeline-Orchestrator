package pipeline.orchestrator.architecture.parsing;

import pipeline.orchestrator.verification.annotations.Verifiable;
import pipeline.orchestrator.verification.annotations.VerifyNotNull;
import pipeline.orchestrator.verification.annotations.VerifyPositive;

/**
 * Class to store the information regarding a stage
 */
@Verifiable
public class StageInformationDto {

    // Identifier for the stage
    @VerifyNotNull
    private String name;

    // Host of the target service
    @VerifyNotNull
    private String host;

    // Port for the target service
    @VerifyPositive
    private int port;

    // Name of the grpc service to invoke
    private String service;

    // Name of the grpc method to invoke
    private String method;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return "StageInformationDto{" +
                "name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", service='" + service + '\'' +
                ", method=" + method +
                '}';
    }
}
