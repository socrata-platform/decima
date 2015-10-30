require 'time'
require 'aws-sdk'
require 'json'

Aws.config[:region] = 'us-west-2'
QUEUE_URL = 'https://sqs.us-west-2.amazonaws.com/710578378071/decima-sqs-staging-DecimaNotificationQueue-1HFFPMJAA6AW1'
# QUEUE_URL = 'https://sqs.us-west-2.amazonaws.com/294784705627/DecimaTest'

deploy = {
  service: 'test-service',
  environment: 'staging',
  version: '1.2.3',
  service_sha: 'asdfasdf',
  deployed_by: 'engineer',
  deployed_at: '2015-10-30T22:16:07Z',
  deploy_method: 'apps-marathon:deploy'
}

puts "Sending deploy: #{deploy.to_json}"

sqs = Aws::SQS::Client.new
resp = sqs.send_message(
  queue_url: QUEUE_URL,
  message_body: deploy.to_json
)
puts "Sent message: #{resp.message_id}"
