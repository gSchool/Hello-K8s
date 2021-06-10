# HelloK8s

Demo application for Spring Boot with Spring Cloud Kubernetes

This app is intended to go along with Unit [02-configmaps-secrets](https://github.com/gSchool/ent-kubernetes/tree/master/02-configmaps-secrets) of the [ent-kubernetes](https://github.com/gSchool/ent-kubernetes) learn block.

## addSecurity branch
This branch is intended to demonstrate how you would add security to a service after it is built.  You can use it in connection with [glab-idp-service](https://gitlab.com/galv-vmw-apr-2021/capstone/glab-idp-service) project or any other JWT based Identity Provide.

Check the diff on this branch / commit to see what I did actually, but here is the cookbook...

1. Copy JwtProperties to retrieve the properties from the configmap and secrets.
1. Copy JwtTokenAuthenticationFilter 
1. Add the configuration (SecurityCredentialsConfig.java).  
    - NOTE the minnimal code here compared to the identity provider
    - Include `@EnableGlobalMethodSecurity(prePostEnabled = true)` if you intend to use annotation based security.
1. In your controller, add the `@PreAuthorize("hasAuthority('ROLE_<role-name>')")` for the controller OR each endpoint.
    - NOTE: `ROLE_` is included in this case.  You may be able to fix this with a constant.  Feel free to experiment.
1. In order to use the jwt-key-secret secret stored on the cluster, you need to set it up in the application.properties file, and in your deploy file.  There are other ways of doing this as well.  Feel free to research. See the two files in this project for my way.

### Endpoints & Security Requirements...

Use `kubectl get service/hello-k8s-secure` to find the endpoint
- `GET /open` No privs required (set in config class).  This endpoint will validate the JWT (if there is one), and reveal the assigned roles if the JWT is valid.
- `GET /hello` ROLE_USER required.
- `GET /secret` ROLE_ADMIN required
- `GET /actuator/health` No privs required - Accessed by K8s to tell if the server is up.
- `GET /actuator/**` ROLE_ADMIN - All actuator endpoints are exposed for this demo.
