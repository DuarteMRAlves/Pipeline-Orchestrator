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

    // Information regarding the method to invoke
    @VerifyNotNull
    private MethodInformationDto method;

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

    public MethodInformationDto getMethod() {
        return method;
    }

    public void setMethod(MethodInformationDto method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return "StageInformationDto{" +
                "name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", method=" + method +
                '}';
    }
}
