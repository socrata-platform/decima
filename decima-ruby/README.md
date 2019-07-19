# Decima Ruby Client
Ruby API wrapper around Decima version service.

## Installation

Add this line to your application's Gemfile:

```ruby
gem 'decima-ruby', git: 'git@github.com:socrata/decima-ruby.git'
```

And then execute:

    $ bundle

Or install it yourself as:

    $ gem install decima-ruby

## Usage

Add this line to your ruby file:
```ruby
require 'decima'
```

### Client

To create a new client:
```ruby
client = Decima::Client.new
```

`#get_deploys`
* returns a list of deployments
* can be filtered by passing a hash with the following keys:
* `environments:` an array of environments to return deploys for
* `services:` an array of services to return deploys for

### Deploy

The `decima-ruby` gem provides a model class for the deploy object. The client returns arrays of `Deploy` objects when possible.

## Development

Pull requests welcome.

Run unit tests with the following command:
```sh
$ rake spec
```
