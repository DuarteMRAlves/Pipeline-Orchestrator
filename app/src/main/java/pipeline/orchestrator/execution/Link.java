package pipeline.orchestrator.execution;

import com.google.protobuf.DynamicMessage;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/*
 * Class to link to stages
 */
public class Link {

    private static final int MAX_QUEUE_SIZE = 1;

    private final String sourceFieldName;
    private final String targetFieldName;
    private final BlockingQueue<DynamicMessage> dataQueue = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE);

    public Link(String sourceFieldName, String targetFieldName) {
        this.sourceFieldName = sourceFieldName;
        this.targetFieldName = targetFieldName;
    }

    public String getSourceFieldName() {
        return sourceFieldName;
    }

    public String getTargetFieldName() {
        return targetFieldName;
    }

    public void put(DynamicMessage dynamicMessage) throws InterruptedException {
        synchronized (dataQueue) {
            if (dataQueue.size() == MAX_QUEUE_SIZE)
                dataQueue.take();
            dataQueue.put(dynamicMessage);
        }
    }

    public DynamicMessage take() throws InterruptedException {
        return dataQueue.take();
    }
}
