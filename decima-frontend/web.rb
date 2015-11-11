require 'sinatra'

get '/'  do
  erb :index
end

get '/deploy/summary' do
  content_type :json
  service = params[:service]
  case service
  when 'core'
    erb :summary_core
  when 'frontend'
    erb :summary_frontend
  else
    erb :summary
  end
end

get '/deploy' do
  content_type :json
  erb :deploy
end

get %r{/service/([\w\-]+)} do
  @service = params[:captures].first
  erb :service
end
