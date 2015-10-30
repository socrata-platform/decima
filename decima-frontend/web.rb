require 'sinatra'

get '/'  do
  erb :index
end

get '/deploy/summary' do
  content_type :json
  erb :summary
end

get '/deploy' do
  content_type :json
  erb :deploy
end
