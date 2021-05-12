package pipeline.orchestrator.base;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class RequestCounter<T> {

    private final int requestLimit;
    private final CountDownLatch countDownLatch;
    private final List<T> received = new ArrayList<>();;
    private int requestCount = 0;

    public RequestCounter(Builder builder) {
        Preconditions.checkArgument(builder.requestLimit > 0);
        this.requestLimit = builder.requestLimit;
        this.countDownLatch = builder.countDownLatch;
    }

    public synchronized void processRequest(T request) {
        requestCount++;
        if (requestCount <= requestLimit) {
            received.add(request);
        }
        if (countDownLatch != null && requestCount == requestLimit) {
            countDownLatch.countDown();
        }
    }

    public ImmutableList<T> getReceivedRequests() {
        return ImmutableList.copyOf(received);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private int requestLimit = -1;
        private CountDownLatch countDownLatch = null;

        private Builder() {}

        public Builder setRequestLimit(int requestLimit) {
            this.requestLimit = requestLimit;
            return this;
        }

        public Builder withCountDownLatch(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
            return this;
        }

        public <T> RequestCounter<T> build() {
            return new RequestCounter<>(this);
        }
    }
}
