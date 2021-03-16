package pipeline.orchestrator.grpc.utils;

/**
 * Static helper class for managing naming on grpc services
 */
public class Naming {

    private Naming() {}

    public static String methodFullName(
            String serviceName,
            String methodName) {

        return String.join("/", serviceName, methodName);
    }

    public static String formatMessageTypeName(String messageType) {
        if (messageType.startsWith(".")) return messageType.substring(1);
        return messageType;
    }
}
