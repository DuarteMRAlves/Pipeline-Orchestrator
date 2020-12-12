package pipeline.orchestrator.architecture.parsing;

import io.grpc.MethodDescriptor.MethodType;
import pipeline.orchestrator.verification.annotations.Verifiable;
import pipeline.orchestrator.verification.annotations.VerifyNotNull;

/**
 * Class to represent the information of the invoked method
 */
@Verifiable
public class MethodInformationDto {

    private String name;

    @VerifyNotNull
    private MethodType type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MethodType getType() {
        return type;
    }

    public void setType(MethodType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "MethodInformationDto{" +
                "name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}
