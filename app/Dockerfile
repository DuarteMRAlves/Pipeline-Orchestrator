FROM openjdk:11

ARG WORKSPACE=/app
ARG USER=runner
ARG GROUP=runner-group

# Create non-privileged user to run
RUN addgroup --system ${GROUP} && \
    adduser --system --no-create-home --ingroup ${GROUP} ${USER} && \
    mkdir ${WORKSPACE} && \
    chown -R ${USER}:${GROUP} ${WORKSPACE}

WORKDIR ${WORKSPACE}

# Add wait-for-* scripts
COPY bin/ ${WORKSPACE}/bin/
RUN chmod -R +x ${WORKSPACE}/bin/

# Add to /usr/bin for backwards compatibility
COPY bin/ /usr/bin/
RUN chmod +x /usr/bin/wait-for-it.sh && \
    chmod +x /usr/bin/wait-for-all.sh

COPY build/libs/ ${WORKSPACE}/

CMD java -cp '*' pipeline.orchestrator.App