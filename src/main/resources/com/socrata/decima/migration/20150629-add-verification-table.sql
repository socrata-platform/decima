create table if not exists "verifications" (
  "id" BIGSERIAL NOT NULL PRIMARY KEY,
  "status" VARCHAR(254) NOT NULL,
  "details" TEXT,
  "time" TIMESTAMP NOT NULL,
  "deploy_id" BIGINT references deploys(id)
)