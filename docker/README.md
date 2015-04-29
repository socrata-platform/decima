# Decima Docker Config #
To build the image, run:
    `docker build -t decima .`

Or, if you want to replace old versions:
    `docker build --rm -t decima .`

## Required Environment Variables ##

## Optional Runtime Variables ##
See the [Dockerfile](Dockerfile) for defaults.

* `JAVA_XMX`                - Sets the JVM heap size.
* `MIN_THREADS`             - Sets the minimum number of server threads.
* `MAX_THREADS`             - Sets the maximum number of server threads.

Note: `MAX_THREADS` should be set so that each thread has about 100m
to use.  There are plans to address this in a future release.
