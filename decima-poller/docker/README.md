# Decima Docker Config #
To build the image, run:
    `docker build -t decima .`

Or, if you want to replace old versions:
    `docker build --rm -t decima .`

## Required Environment Variables ##

## Optional Runtime Variables ##
See the [Dockerfile](Dockerfile) for defaults.

* `JAVA_XMX`                - Sets the JVM heap size.
