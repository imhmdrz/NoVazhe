FROM eclipse-temurin:21-jre-jammy

ENV APP_HOME=/app \
    CONTENT_DIR=/app/content

WORKDIR ${APP_HOME}

COPY server/build/install/server/ ./
COPY server/content/ ./content/

EXPOSE 8080

ENTRYPOINT ["/app/bin/server"]
