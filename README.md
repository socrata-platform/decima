# decima #
A service to track deployments and service versions across environments.

[![Build Status](https://travis-ci.org/socrata-platform/decima.svg?branch=master)](https://travis-ci.org/socrata-platform/decima)

Most of the documentation is kept on [apiary](http://docs.decima.apiary.io).

## Build & Run ##

Locally:
```sh
$ sbt
> decima-http/container:start
> decima-http/browse
```

If `browse` doesn't launch your browser, manually open [http://localhost:8080/](http://localhost:8080/) in your browser.

* auto-compile on save: `> ~ ;copy-resources;aux-compile`

Docker:
* See the Docker [README](docker/README.md) for information on creating the container.
* The service runs on port 7474, or in Marathon at http://decima.app.marathon.[environment].socrata.net

## Querying version and deployment info ##
* **/deploy**: returns a JSON array of the current state of services in all environments.
* **/deploy/history**: returns a JSON array of the last N deploys (defaults to 100)

#### Query Params ####
* environment: filter by environment
* service: filter by service name
* limit: number of deploys to return (history only)

## Reporting a deploy event ##

PUT to /deploy endpoint with a body containing the relevant information
```sh
curl -X PUT -H 'Content-Type: application/json' \
    -d '{"service": "core", "environment": "staging", "version": "1.2.4", "service_sha": "asdwerasdf", "deploy_method": "autoprod", "deployed_by": "an engineer"}' \
    http://localhost:8080/deploy
```

## Setup Database ##

First, set up the Decima database. The default in reference.conf is a database named `decima` with user/password both equal to 'blist'.

Decima uses a `deploys` table, to create this on your local machine execute the following (this will not set up the database, only the necessary tables):

```bash
$ java -cp target/scala-2.11/decima-assembly-*.jar com.socrata.decima.MigrateSchema migrate
```

Or, just run the migrations by executing `bin/run_migrations.sh` with optional arguments: `migrate (default), redo [n], undo [n]`.
