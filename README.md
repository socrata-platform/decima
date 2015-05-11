# decima #
A service to track deployments and service versions across environments.

## Build & Run ##

Locally:
```sh
$ ./sbt
> container:start
> browse
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
    -d '{"service": "core", "environment": "staging", "version": "1.2.4", "revision": "optional", "deploy_method": "autoprod", "deployed_by": "an engineer"}' \
    http://localhost:8080/deploy
```

## Setup Database ##

Decima expects a `deploys` table, to create this on your local machine execute the following:
```bash
$ java -cp target/scala-2.11/decima-assembly-0.1.2-SNAPSHOT.jar com.socrata.decima.Bootstrap
```

```sql

```

## Questions / TODOs ##
* Configuration:
    * c3p0
    * Docker
    * Postgres vs. RDS
* RDS
* Docker deployment
*
