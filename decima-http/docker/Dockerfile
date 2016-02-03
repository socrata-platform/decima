FROM socrata/runit-java8

# Service specific settings.
ENV PROJECT_NAME decima-http
ENV SERVICE_PORT 7474
EXPOSE 7474

# Environment Settings.
ENV JAVA_XMX 512m

# Some paths/locations we care about
ENV SERVICE_ROOT      /srv/${PROJECT_NAME}/
ENV SERVICE_CONFIG    ${PROJECT_NAME}.conf
ENV LOGBACK_CONFIG    ${PROJECT_NAME}-logback.xml
ENV SERVICE_ARTIFACT  ${PROJECT_NAME}-assembly.jar

# DB Settings
ENV PG_DB_HOST localhost
ENV PG_DB_PORT 5432
ENV PG_DB_NAME decima
ENV PG_DB_USER decima
ENV PG_DB_PASSWORD_LINE ""

# AWS S3 Settings
ENV AWS_CREDENTIALS_LINE ""
ENV BUILD_ARTIFACTS_BUCKET "socrata-build-artifacts-us-west-2"

# Log Settings.
ENV LOG_LEVEL info

# Add other required resources to the container
COPY ${LOGBACK_CONFIG} /etc/
COPY ${SERVICE_CONFIG}.j2 /etc/
COPY ${SERVICE_ARTIFACT} ${SERVICE_ROOT}
COPY sv/decima-http-run /etc/service/decima-http/run
