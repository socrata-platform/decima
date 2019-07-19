module Decima
  class Deploy
    attr_accessor :configuration, :deployed_at, :deployed_by, :deploy_method, :docker_sha, :docker_tag, :environment,
                  :id, :service, :service_sha, :version, :verified

    def initialize(d)
      @configuration = d['configuration']
      @deployed_at   = Time.parse(d['deployed_at']) unless d['deployed_at'].nil?
      @deployed_by   = d['deployed_by']
      @deploy_method = d['deploy_method']
      @docker_sha    = d['docker_sha']
      @docker_tag    = d['docker_tag']
      @environment   = d['environment']
      @id            = d['id']
      @service       = d['service']
      @service_sha   = d['service_sha']
      @verified      = d['verified']
      @version       = d['version']
    end

    ## utility methods

    def diff(deploy)
      if deploy.nil?
        return 'not found'
      end
      diffs = []
      if version != deploy.version
        diffs << "ver: #{deploy.version}"
      end
      if service_sha != deploy.service_sha
        diffs << "sha: #{deploy.service_sha}"
      end
      if docker_tag != deploy.docker_tag
        diffs << "tag: #{deploy.docker_tag}"
      end
      if diffs.size > 0
        return diffs.join(', ')
      else
        return 'match'
      end
    end

    def to_s
      "#{environment}:#{service}@#{version}"
    end

    def to_hash
      Hash[
        instance_variables.reject { |key| instance_variable_get(key).nil? }.map do |key|
          [ key.to_s.delete('@'), instance_variable_get(key) ]
        end
      ].tap do |hash|
        hash['deployed_at'] = deployed_at.iso8601 unless deployed_at.nil?
      end
    end

    def version_match?(deploy)
      ret = self.version == deploy.version &&
            self.service_sha == deploy.service_sha
      unless self.docker_tag.nil? || deploy.docker_tag.nil?
        ret = ret && self.docker_tag == deploy.docker_tag
      end
      ret
    end

  end
end
