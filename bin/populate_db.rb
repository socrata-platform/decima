require 'httparty'
require 'json'

deploys = [
  { service: 'core', environment: 'staging', version: '1.1.0', git: 'blah', deployed_by: 'autoprod' },
  { service: 'core', environment: 'staging', version: '1.1.1', git: 'blah', deployed_by: 'autoprod' },
  { service: 'core', environment: 'staging', version: '1.1.2', git: 'blah', deployed_by: 'autoprod' },
  { service: 'core', environment: 'rc', version: '1.1.1', git: 'blah', deployed_by: 'autoprod' },
  { service: 'core', environment: 'staging', version: '1.1.3', git: 'blah', deployed_by: 'autoprod' },
  { service: 'core', environment: 'staging', version: '1.1.5', git: 'blah', deployed_by: 'autoprod' },
  { service: 'core', environment: 'rc', version: '1.1.3', git: 'blah', deployed_by: 'autoprod' },
  { service: 'core', environment: 'production', version: '1.1.1', git: 'blah', deployed_by: 'autoprod' },
  { service: 'core', environment: 'rc', version: '1.1.5', git: 'blah', deployed_by: 'autoprod' },
  { service: 'phidippides', environment: 'staging', version: '0.2.0', git: 'blah', deployed_by: 'autoprod' },
  { service: 'phidippides', environment: 'staging', version: '0.2.1', git: 'blah', deployed_by: 'autoprod' },
  { service: 'phidippides', environment: 'staging', version: '0.2.2', git: 'blah', deployed_by: 'autoprod' },
  { service: 'phidippides', environment: 'rc', version: '0.2.1', git: 'blah', deployed_by: 'autoprod' },
  { service: 'phidippides', environment: 'staging', version: '0.2.3', git: 'blah', deployed_by: 'autoprod' },
  { service: 'phidippides', environment: 'staging', version: '0.2.5', git: 'blah', deployed_by: 'autoprod' },
  { service: 'phidippides', environment: 'rc', version: '0.2.3', git: 'blah', deployed_by: 'autoprod' },
  { service: 'phidippides', environment: 'production', version: '0.2.1', git: 'blah', deployed_by: 'autoprod' },
  { service: 'phidippides', environment: 'rc', version: '0.2.5', git: 'blah', deployed_by: 'autoprod' },
  { service: 'frontend', environment: 'staging', version: '0.11.0', git: 'blah', deployed_by: 'autoprod' },
  { service: 'frontend', environment: 'staging', version: '0.11.1', git: 'blah', deployed_by: 'autoprod' },
  { service: 'frontend', environment: 'staging', version: '0.11.2', git: 'blah', deployed_by: 'autoprod' },
  { service: 'frontend', environment: 'rc', version: '0.11.1', git: 'blah', deployed_by: 'autoprod' },
  { service: 'frontend', environment: 'staging', version: '0.11.3', git: 'blah', deployed_by: 'autoprod' },
  { service: 'frontend', environment: 'staging', version: '0.11.5', git: 'blah', deployed_by: 'autoprod' },
  { service: 'frontend', environment: 'rc', version: '0.11.3', git: 'blah', deployed_by: 'autoprod' },
  { service: 'frontend', environment: 'production', version: '0.11.1', git: 'blah', deployed_by: 'autoprod' },
  { service: 'frontend', environment: 'rc', version: '0.11.5', git: 'blah', deployed_by: 'autoprod' }
]

deploys.each do |d|
  puts 'adding deploy event: ' + d.to_json
  HTTParty.put(
    'http://localhost:8080/deploy',
    body: d.to_json,
    headers: { 'Content-Type' => 'application/json' }
  )
  sleep 1
end
