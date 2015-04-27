# decima #

## Build & Run ##

```sh
$ ./sbt
> container:start
> browse
```

If `browse` doesn't launch your browser, manually open [http://localhost:8080/](http://localhost:8080/) in your browser.

* auto-compile on save: `> ~ ;copy-resources;aux-compile`

## Create tables ##

```sql
create table deploys (
    id serial primary key,
    service varchar(128),
    environment varchar(128),
    version varchar(128),
    git varchar(128),
    timestamp timestamp default now());
```

## Queries ##

Get the latest versions of each service in each environment:
```sql
select a.id, a.service, a.environment, b.version, b.git, b.timestamp
from (
select distinct deploys.service, deploys.environment, max(deploys.id) as id
from deploys
group by deploys.service, deploys.environment) a, deploys b
where a.id = b.id;
```

## Reporting a deploy event ##

PUT to /deploy endpoint with a body containing the relevant information
```sh
curl -X PUT -H 'Content-Type: application/json' \
    -d '{"service": "core", "environment": "staging", "version": "1.2.4", "git": "optional"}' \
    http://localhost:8080/deploy
```

## Questions / TODOs ##
* Configuration:
    * c3p0
    * Docker
    * Postgres vs. RDS
* RDS
* Docker deployment
* 
