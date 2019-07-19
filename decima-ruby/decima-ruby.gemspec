# coding: utf-8
lib = File.expand_path('../lib', __FILE__)
$LOAD_PATH.unshift(lib) unless $LOAD_PATH.include?(lib)
require 'decima/version'

Gem::Specification.new do |spec|
  spec.name          = "decima-ruby"
  spec.version       = Decima::VERSION
  spec.authors       = ["Michael Brown"]
  spec.email         = ["michael.brown@socrata.com"]

  spec.summary       = %q{Ruby API wrapper around the Decima API}
  spec.description   = %q{Provides a client and deploy model object for interacting with the version and deployment tracking service, Decima}
  spec.homepage      = "https://github.com/socrata/decima-ruby"
  spec.license       = "MIT"

  spec.files         = `git ls-files -z`.split("\x0").reject { |f| f.match(%r{^(test|spec|features)/}) }
  spec.bindir        = "exe"
  spec.executables   = spec.files.grep(%r{^exe/}) { |f| File.basename(f) }
  spec.require_paths = ["lib"]

  if spec.respond_to?(:metadata)
    spec.metadata['allowed_push_host'] = "TODO: Set to 'http://mygemserver.com' to prevent pushes to rubygems.org, or delete to allow pushes to any server."
  end

  spec.add_dependency 'addressable', '~> 2.3'
  spec.add_dependency 'httparty', '~> 0.13'

  spec.add_development_dependency 'simplecov', '~> 0.10'
  spec.add_development_dependency "webmock", '~> 1.21'
  spec.add_development_dependency "bundler", "~> 1.9"
  spec.add_development_dependency "rake", "~> 10.0"
  spec.add_development_dependency "rspec", '~> 3.3'
end
