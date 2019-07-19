require_relative 'deploy'
require 'addressable/uri'
require 'httparty'
require 'json'

module Decima
  class Client
    class << self
      DEFAULT_DECIMA_URI = 'http://decima.app.marathon.aws-us-west-2-infrastructure.socrata.net'

      # Creates a new Decima::Client
      #
      # @param options [Hash] A hash with optional parameters for creating a new Decima::Client.
      #
      # Optional params include:
      #   :decima_uri [String] The base uri to use for accessing Decima. Defaults to DEFAULT_DECIMA_URI.
      #   :debug [Boolean] If true, the debug_output will be printed to STDOUT.
      def new(options = {})
        client_class = Class.new(AbstractClient) do |klass|
          decima_uri = options[:decima_uri] || DEFAULT_DECIMA_URI
          decima_uri = "http://#{decima_uri}" unless decima_uri.start_with?('http')
          uri = Addressable::URI.parse(decima_uri).to_s
          klass.base_uri(uri)
          klass.debug_output($stdout) if options[:debug]
        end
        client_class.new
      end
    end

    class AbstractClient
      include HTTParty

      # Makes a GET request to the /deploy endpoint
      #
      # @param opts [Hash] A hash with optional query parameters to filter the GET request.
      #
      # Optional parameters:
      #   :environments [Array] List of environments to return deploy events for.
      #   :services [Array] List of services to return deploy events for.
      def get_deploys(opts = {})
        query_params = {}
        unless opts[:environments].nil?
          query_params['environment'] = opts[:environments].join(',')
          end
        unless opts[:services].nil?
          query_params['service'] = opts[:services].join(',')
        end
        response = perform_get("/deploy", query: query_params)
#        fail("Invalid response, code: #{response.code}\n#{response.body}") unless response.code == 200
        begin
          JSON.parse(response.body).map { |d| Deploy.new(d) }
        rescue StandardError => e
          warn("Error: Unable to parse response as JSON, response body:\n#{e}")
          raise
        end
      end

      private

      def perform_get(path, opts = {})
        response = self.class.get(path, opts)
        handle_error(:get, path, opts, response) unless response.code == 200
        response
      end

      def handle_error(action, path, options, response)
        fail("Invalid response to #{action} request to #{path} with #{options}\nResponse code: #{response.code}\n#{response.body}")
      end
    end
  end
end
