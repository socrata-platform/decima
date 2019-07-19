$LOAD_PATH.unshift File.expand_path('../../lib', __FILE__)

require 'simplecov'

SimpleCov.formatters = [SimpleCov::Formatter::HTMLFormatter]

SimpleCov.start do
  add_filter '/spec/'
  #  minimum_coverage(99.61)
end

require 'decima'
require 'webmock/rspec'

WebMock.disable_net_connect!

RSpec.configure do |config|
  config.expect_with :rspec do |expectations|
    expectations.include_chain_clauses_in_custom_matcher_descriptions = true
  end

  config.mock_with :rspec do |mocks|
    mocks.verify_partial_doubles = true
  end


  def a_get(path)
    a_request(:get, path)
  end

  def a_post(path)
    a_request(:post, path)
  end

  def a_delete(path)
    a_request(:delete, path)
  end

  def stub_get(path)
    stub_request(:get, path)
  end

  def stub_post(path)
    stub_request(:post, path)
  end

  def stub_delete(path)
    stub_request(:delete, path)
  end

end

def fixture_path
  File.expand_path('../fixtures', __FILE__)
end

def fixture(file)
  File.new(File.join(fixture_path, file))
end
