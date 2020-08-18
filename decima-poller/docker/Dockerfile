FROM socrata/java8-bionic

# Service specific settings.
ENV PROJECT_NAME decima-poller

# Environment Settings.
ENV JAVA_XMX 512m

# Some paths/locations we care about
ENV SERVICE_ROOT      /srv/${PROJECT_NAME}/
ENV SERVICE_CONFIG    ${PROJECT_NAME}.conf
ENV LOGBACK_CONFIG   ${PROJECT_NAME}-logback.xml
ENV SERVICE_ARTIFACT  ${PROJECT_NAME}-assembly.jar

# DB Settings
ENV PG_DB_HOST localhost
ENV PG_DB_PORT 5432
ENV PG_DB_NAME decima
ENV PG_DB_USER decima
ENV PG_DB_PASSWORD_LINE ""

# AWS SQS Settings
ENV AWS_CREDENTIALS_LINE ""
ENV SQS_BASE_ENDPOINT ""
ENV SQS_QUEUE_NAME "DecimaTest"

# Akka Settings
ENV LOG_LEVEL "info"

# Log Settings.
ENV LOG_LEVEL info

# Add other required resources to the container
ADD ship.d /etc/ship.d
ADD ${LOGBACK_CONFIG} /etc/
ADD ${SERVICE_CONFIG}.j2 /etc/
ADD ${SERVICE_ARTIFACT} ${SERVICE_ROOT}
