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
# ENV MIN_THREADS 3
# ENV MAX_THREADS 5               # Threads currently use about 100m each.

# DB Settings
ENV PG_DB_HOST localhost
ENV PG_DB_PORT 5432
ENV PG_DB_NAME decima
ENV PG_DB_USER decima

# Log Settings.
ENV LOG_LEVEL info
# ENV LOG_PATTERN %d %-5p [%t]  \(%X{X-Socrata-RequestId}\) [%X{X-Socrata-Resource}] %c{36} "%X{X-Socrata-Host}" %m%n

# Add other required resources to the container
ADD ship.d /etc/ship.d
ADD ${SERVER_CONFIG}.j2 /etc/
ADD ${LOGBACK_CONFIG}.j2 /etc/
ADD ${SERVER_ARTIFACT} ${SERVER_ROOT}

