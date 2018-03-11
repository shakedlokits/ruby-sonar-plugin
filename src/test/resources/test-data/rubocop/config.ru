# This file is used by Rack-based servers to start the application.

require ::File.expand_path('../config/environment', __FILE__)

class ScriptName
  def initialize(app)
    @app = app
  end

  def call(env)
    Rails.application.set_script_name(env['HTTP_FRONTEND_URI'].to_s)
    env['SCRIPT_NAME'] += env['HTTP_FRONTEND_URI'].to_s if !Rails.env.development?
    @app.call(env)
  end
end

use ScriptName

map Rails.application.root_relative_path do
  run Rails.application
end
