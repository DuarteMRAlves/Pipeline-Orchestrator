package pipeline.orchestrator.grpc.reflection;

import io.grpc.reflection.v1alpha.ServerReflectionRequest;

/**
 * Factory to create messages for grpc-reflection
 */
public class ReflectionRequestFactory {

    private static final ServerReflectionRequest.Builder LIST_SERVICES_BUILDER
            = ServerReflectionRequest.newBuilder();

    private static final ServerReflectionRequest.Builder FILE_BY_FILENAME_BUILDER
            = ServerReflectionRequest.newBuilder();

    private static final ServerReflectionRequest.Builder FILE_BY_SYMBOL_BUILDER
            = ServerReflectionRequest.newBuilder();

    private ReflectionRequestFactory() {}

    /**
     * Creates a message the should be used to list
     * the services of the grpc server
     * @param host host of the server
     * @return the request that should be used
     */
    public static ServerReflectionRequest listServicesRequest(String host) {
        return LIST_SERVICES_BUILDER.setHost(host).setListServices("").build();
    }

    /**
     * Creates a message the should be used to
     * ask for a protobuf file with a given name
     * @param host host of the server
     * @param filename name filename to request
     * @return the request that should be used
     */
    public static ServerReflectionRequest fileByFilenameRequest(String host, String filename) {
        return FILE_BY_FILENAME_BUILDER.setHost(host).setFileByFilename(filename).build();
    }

    /**
     * Creates a message the should be used to
     * ask for a protobuf file with a given symbol
     * @param host host of the server
     * @param symbol symbol that the file should contain
     * @return the request that should be used
     */
    public static ServerReflectionRequest fileBySymbolRequest(String host, String symbol) {
        return FILE_BY_SYMBOL_BUILDER.setHost(host).setFileContainingSymbol(symbol).build();
    }
}
