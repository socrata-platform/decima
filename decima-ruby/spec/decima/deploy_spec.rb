require 'spec_helper'

describe Decima::Deploy do
  let(:deploy_hash) {
    {
      "id" => 8,
      "service" => "core",
      "environment" => "production",
      "version" => "1.1.1",
      "service_sha" => "blah",
      "docker_tag" => "1.1.1_345_blah",
      "docker_sha" => "foobar",
      "configuration" => "{ \"this\": \"is a config file\" }",
      "deployed_by" => "an engineer",
      "deploy_method" => "autoprod",
      "deployed_at" => "2015-05-26T18:20:50Z"
    }
  }
  let(:partial_hash) {
    {
      "id" => 8,
      "service" => "core",
      "environment" => "production",
      "version" => "1.1.1",
      "service_sha" => "blah",
      "deployed_by" => "an engineer",
      "deploy_method" => "autoprod",
      "deployed_at" => "2015-05-26T18:20:50Z"
    }
  }
  let(:deploy) { Decima::Deploy.new(deploy_hash) }

  it 'is created with deploy hash' do
    expect { Decima::Deploy.new(deploy_hash) }.to_not raise_error
  end

  it 'can be modified' do
    new_service = 'core-123'
    deploy.service = new_service
    expect(deploy.service).to eq(new_service)
  end

  describe('#to_hash') do
    it 'can be serialized back to the same hash' do
      expect(deploy.to_hash).to eq(deploy_hash)
    end

    it 'can be serialized with optional params missing' do
      partial_deploy = Decima::Deploy.new(partial_hash)
      expect(partial_deploy.to_hash).to eq(partial_hash)
    end
  end

  describe('#to_s') do
    it 'is converted to a human-readable string' do
      expect(deploy.to_s).to eq('production:core@1.1.1')
    end
  end
end
