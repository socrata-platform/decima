FROM socrata/java

# Service specific settings.
ENV SERVER_NAME decima
ENV SERVICE_PORT 7474
EXPOSE 7474

# Some paths/locations we care about
ENV SERVER_ROOT      /srv/${SERVER_NAME}/
ENV SERVER_CONFIG    ${SERVER_NAME}.conf
ENV LOGBACK_CONFIG   ${SERVER_NAME}-logback.xml
ENV SERVER_ARTIFACT  ${SERVER_NAME}-assembly.jar

# Environment Settings.
ENV JAVA_XMX 512m

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
ADD ship.d /etc/ship.d
ADD ${SERVER_CONFIG}.j2 /etc/
ADD ${LOGBACK_CONFIG}.j2 /etc/
ADD ${SERVER_ARTIFACT} ${SERVER_ROOT}
