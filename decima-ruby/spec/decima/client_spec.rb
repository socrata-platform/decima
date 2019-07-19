require 'spec_helper'

describe Decima::Client do
  let(:base_uri) { 'http://decima' }
  let(:client) { Decima::Client.new(decima_uri: base_uri) }

  it 'is created with no parameters' do
    expect { Decima::Client.new }.to_not raise_error
  end

  describe '#get_deploys' do
    before do
      stub_get("#{base_uri}/deploy").to_return(body: fixture('deploys.json'))
      stub_get("#{base_uri}/deploy?service=core,frontend").to_return(body: fixture('deploys.json'))
      stub_get("#{base_uri}/deploy?environment=rc,production").to_return(body: fixture('deploys.json'))
    end

    it 'makes a basic request with no parameters' do
      client.get_deploys
      expect(a_get("#{base_uri}/deploy")).to have_been_made
    end

    it 'filters requests with a list of services' do
      client.get_deploys(services: [ 'core', 'frontend' ])
      expect(a_get("#{base_uri}/deploy?service=core,frontend")).to have_been_made
    end

    it 'filters reqests with a list of environments' do
      client.get_deploys(environments: [ 'rc', 'production' ])
      expect(a_get("#{base_uri}/deploy?environment=rc,production")).to have_been_made
    end
  end
end
