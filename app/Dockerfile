FROM openjdk:11

RUN mkdir /app

# Add wait for it script for convenience
COPY bin/ /usr/bin/
RUN chmod +x /usr/bin/wait-for-it.sh && \
    chmod +x /usr/bin/wait-for-all.sh

COPY build/libs/ /app/

WORKDIR /app

CMD java -cp '*' pipeline.orchestrator.App