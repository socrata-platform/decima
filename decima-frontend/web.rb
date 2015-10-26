require 'sinatra'

get '/'  do
  erb :index
end

get '/deploy' do
  content_type :json
  erb :deploy
end
