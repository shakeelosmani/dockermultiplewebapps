# dockermultiplewebapps
Simple docker application using Python, Flask, uWSGI, and Nginx to demonstrate how to deploy multiple web applications.
 - Uses docker-compose
 - uWSGI as application server
 - Flask for REST endpoint
 - Web Application 1 listens at a context /hello - requests can be /hello/:name - response JSON "Hello {NAME}"
 - Web Application 2 listens at a context /parse - requests can be /parse/:name - responsds with JSON name parsed into parts
 - If you wish to deploy it to a swarm use `dcoker-compose-stack-deploy.yml`
 - Of course change the nginx.conf uwsgi_pass according to the service name and you are all set
Behind the scene it uses the [name parser library](https://pypi.org/project/nameparser/)