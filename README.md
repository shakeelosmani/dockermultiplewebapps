# dockermultiplewebapps
Simple docker application using Python, Flask, uWSGI, and Nginx to demonstrate how to deploy multiple web applications.
 - Uses docker-compose
 - uWSGI as application server
 - Flask for REST endpoint
 - Web Application 1 listens at a context /hello - requests can be /hello<name> - response Hello <name>
 - Web Application 2 listens at a context /parse - requests can be /parse<human name> - responsds with JSON name parsed into parts  
Behind the scene it uses the [name parser library](https://pypi.org/project/nameparser/)