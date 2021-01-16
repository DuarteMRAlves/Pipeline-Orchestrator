package pipeline.orchestrator.grpc.reflection;

import io.grpc.reflection.v1alpha.ServerReflectionRequest;
import org.junit.Test;

import static org.junit.Assert.*;

public class ReflectionRequestFactoryTest {

    private static final String HOST = "localhost";
    private static final String SYMBOL = "symbol";
    private static final String FILENAME = "filename";

    @Test
    public void listServicesRequestTest() {
        ServerReflectionRequest factoryRequest =
                ReflectionRequestFactory.listServicesRequest(HOST);
        assertEquals(HOST, factoryRequest.getHost());
        assertNotNull(factoryRequest.getListServices());
    }

    @Test
    public void fileBySymbolRequestTest() {
        ServerReflectionRequest factoryRequest =
                ReflectionRequestFactory.fileBySymbolRequest(HOST, SYMBOL);
        assertEquals(HOST, factoryRequest.getHost());
        assertEquals(SYMBOL, factoryRequest.getFileContainingSymbol());
    }

    @Test
    public void fileByFilenameRequestTest() {
        ServerReflectionRequest factoryRequest =
                ReflectionRequestFactory.fileByFilenameRequest(HOST, FILENAME);
        assertEquals(HOST, factoryRequest.getHost());
        assertEquals(FILENAME, factoryRequest.getFileByFilename());
    }
}
