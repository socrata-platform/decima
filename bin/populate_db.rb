require 'httparty'
require 'json'

deploys = [
  { service: 'core', environment: 'staging', version: '1.1.0', service_sha: 'blah', docker_sha: 'foobar', configuration: '{ "this": "is a config file" }', deployed_by: 'an engineer', deploy_method: 'autoprod' },
  { service: 'core', environment: 'staging', version: '1.1.1', service_sha: 'blah', docker_sha: 'foobar', configuration: '{ "this": "is a config file" }', deployed_by: 'an engineer', deploy_method: 'autoprod' },
  { service: 'core', environment: 'staging', version: '1.1.2', service_sha: 'blah', docker_sha: 'foobar', configuration: '{ "this": "is a config file" }', deployed_by: 'an engineer', deploy_method: 'autoprod' },
  { service: 'core', environment: 'rc', version: '1.1.1', service_sha: 'blah', docker_sha: 'foobar', configuration: '{ "this": "is a config file" }', deployed_by: 'an engineer', deploy_method: 'autoprod' },
  { service: 'core', environment: 'staging', version: '1.1.3', service_sha: 'blah', docker_sha: 'foobar', configuration: '{ "this": "is a config file" }', deployed_by: 'an engineer', deploy_method: 'autoprod' },
  { service: 'core', environment: 'staging', version: '1.1.5', service_sha: 'blah', docker_sha: 'foobar', configuration: '{ "this": "is a config file" }', deployed_by: 'an engineer', deploy_method: 'autoprod' },
  { service: 'core', environment: 'rc', version: '1.1.3', service_sha: 'blah', docker_sha: 'foobar', configuration: '{ "this": "is a config file" }', deployed_by: 'an engineer', deploy_method: 'autoprod' },
  { service: 'core', environment: 'production', version: '1.1.1', service_sha: 'blah', docker_sha: 'foobar', configuration: '{ "this": "is a config file" }', deployed_by: 'an engineer', deploy_method: 'autoprod' },
  { service: 'core', environment: 'rc', version: '1.1.5', service_sha: 'blah', docker_sha: 'foobar', configuration: '{ "this": "is a config file" }', deployed_by: 'an engineer', deploy_method: 'autoprod' },
  { service: 'phidippides', environment: 'staging', version: '0.2.0', service_sha: 'blah', docker_sha: 'foobar', configuration: '{ "this": "is a config file" }', deployed_by: 'an engineer', deploy_method: 'autoprod' },
  { service: 'phidippides', environment: 'staging', version: '0.2.1', service_sha: 'blah', docker_sha: 'foobar', configuration: '{ "this": "is a config file" }', deployed_by: 'an engineer', deploy_method: 'autoprod' },
  { service: 'phidippides', environment: 'staging', version: '0.2.2', service_sha: 'blah', docker_sha: 'foobar', configuration: '{ "this": "is a config file" }', deployed_by: 'an engineer', deploy_method: 'autoprod' },
  { service: 'phidippides', environment: 'rc', version: '0.2.1', service_sha: 'blah', docker_sha: 'foobar', configuration: '{ "this": "is a config file" }', deployed_by: 'an engineer', deploy_method: 'autoprod' },
  { service: 'phidippides', environment: 'staging', version: '0.2.3', service_sha: 'blah', docker_sha: 'foobar', configuration: '{ "this": "is a config file" }', deployed_by: 'an engineer', deploy_method: 'autoprod' },
  { service: 'phidippides', environment: 'staging', version: '0.2.5', service_sha: 'blah', docker_sha: 'foobar', configuration: '{ "this": "is a config file" }', deployed_by: 'an engineer', deploy_method: 'autoprod' },
  { service: 'phidippides', environment: 'rc', version: '0.2.3', service_sha: 'blah', docker_sha: 'foobar', configuration: '{ "this": "is a config file" }', deployed_by: 'an engineer', deploy_method: 'autoprod' },
  { service: 'phidippides', environment: 'production', version: '0.2.1', service_sha: 'blah', docker_sha: 'foobar', configuration: '{ "this": "is a config file" }', deployed_by: 'an engineer', deploy_method: 'autoprod' },
  { service: 'phidippides', environment: 'rc', version: '0.2.5', service_sha: 'blah', docker_sha: 'foobar', configuration: '{ "this": "is a config file" }', deployed_by: 'an engineer', deploy_method: 'autoprod' },
  { service: 'frontend', environment: 'staging', version: '0.11.0', service_sha: 'blah', docker_sha: 'foobar', configuration: '{ "this": "is a config file" }', deployed_by: 'an engineer', deploy_method: 'autoprod' },
  { service: 'frontend', environment: 'staging', version: '0.11.1', service_sha: 'blah', docker_sha: 'foobar', configuration: '{ "this": "is a config file" }', deployed_by: 'an engineer', deploy_method: 'autoprod' },
  { service: 'frontend', environment: 'staging', version: '0.11.2', service_sha: 'blah', docker_sha: 'foobar', configuration: '{ "this": "is a config file" }', deployed_by: 'an engineer', deploy_method: 'autoprod' },
  { service: 'frontend', environment: 'rc', version: '0.11.1', service_sha: 'blah', docker_sha: 'foobar', configuration: '{ "this": "is a config file" }', deployed_by: 'an engineer', deploy_method: 'autoprod' },
  { service: 'frontend', environment: 'staging', version: '0.11.3', service_sha: 'blah', docker_sha: 'foobar', configuration: '{ "this": "is a config file" }', deployed_by: 'an engineer', deploy_method: 'autoprod' },
  { service: 'frontend', environment: 'staging', version: '0.11.5', service_sha: 'blah', docker_sha: 'foobar', configuration: '{ "this": "is a config file" }', deployed_by: 'an engineer', deploy_method: 'autoprod' },
  { service: 'frontend', environment: 'rc', version: '0.11.3', service_sha: 'blah', docker_sha: 'foobar', configuration: '{ "this": "is a config file" }', deployed_by: 'an engineer', deploy_method: 'autoprod' },
  { service: 'frontend', environment: 'production', version: '0.11.1', service_sha: 'blah', docker_sha: 'foobar', configuration: '{ "this": "is a config file" }', deployed_by: 'an engineer', deploy_method: 'autoprod' },
  { service: 'frontend', environment: 'rc', version: '0.11.5', service_sha: 'blah', docker_sha: 'foobar', configuration: '{ "this": "is a config file" }', deployed_by: 'an engineer', deploy_method: 'autoprod' }
]

deploys.each do |d|
  puts 'adding deploy event: ' + d.to_json
  HTTParty.put(
    'http://localhost:8080/deploy',
#    'http://decima.app.marathon.aws-us-west-2-staging.socrata.net/deploy',
    body: d.to_json,
    headers: { 'Content-Type' => 'application/json' }
  )
  sleep 1
end
