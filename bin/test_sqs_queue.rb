require 'time'
require 'aws-sdk'
require 'json'

Aws.config[:region] = 'us-west-2'
QUEUE_URL = 'https://sqs.us-west-2.amazonaws.com/710578378071/decima-sqs-staging-DecimaNotificationQueue-1HFFPMJAA6AW1'
# QUEUE_URL = 'https://sqs.us-west-2.amazonaws.com/294784705627/DecimaTest'

stack_name = 'decima-sqs-staging'
puts "Discovering SQS queue from #{stack_name}"
cfc = Aws::CloudFormation::Client.new
stack = cfc.describe_stacks(
  stack_name: stack_name
).stacks.first
queue_url = stack.outputs.select { |output| output.output_key == 'DecimaNotificationQueueUrl' }.first.output_value

puts "Using queue: #{queue_url}"

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
  queue_url: queue_url,
  message_body: deploy.to_json
)
puts "Sent message: #{resp.message_id}"
