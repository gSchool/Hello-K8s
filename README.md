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


## Unit Testing Concerns

As things stand, there are some issues with Unit tests.  Because we are using JWT, we cannot rely on the standard @MockUser testing 
protocols.  Also, we have a circular dependency bug in our SecurityCredentialsConfig class that needs to be addressed 
before we can do anything. 

### Fix our bug
To fix the circular dependency bug, remove the bean declaration for JwtProperties in SecurityCredentialsConfig, and place it in another configuration 
file, like your *Application I have done this in HelloK8sApplication.properties.  This will separate the bean from it's usage, thereby removing the 
"circular" dependency.

### Add a private method to create a dummy JWT token
Since our application only uses tokens, we can just "dummy" up a token to test with on the fly.  I have implemented a method in my
HelloControllerTest class, and it looks like this...

```java 
    private String getToken(String username, List<String> roles){
        String token = Jwts.builder()
                .setHeaderParam("typ","JWT")
                .setSubject(username)
                .claim("name", username)
                .claim("guid", 99)
                // Convert to list of strings.
                // This is important because it affects the way we get them back in the Gateway.
                .claim("authorities", roles)
//                .setIssuedAt(new Date(now))
//                .setExpiration(new Date(now + jwtProperties.getExpiration() * 1000L))  // in milliseconds
                .signWith(SignatureAlgorithm.HS512, JWT_KEY.getBytes())
                .compact();

        return String.format("Bearer %s", token);
    }
```

This method requires the same JWT_KEY that the token filter will use when validating the user.  I have
accomplished this by creating an application-test.properties file with a value, and then grabbing this 
value at the top of the test with the following code...
```java
    @Value("${security.jwt.secret}")
    String JWT_KEY;
```
Note that I have commented out some of the un-necessary elements of the token.  Feel free to add them 
back if you need them.  Don't forget the `@TestPropertySource(locations="classpath:application-test.properties")`
annotation to read the test properties for the test.

### Creating a test
With these elements, you can now build a test with an Authorization header.  You would add this header
as part of your get(), post(), etc argument to mockMvc.perform as I have done below...

```java
    @Test
    void sayHello() throws Exception {
        String token = getToken("user", Arrays.asList("ROLE_USER"));
        when(myConfig.getMessage()).thenReturn("Sample Message");
        mockMvc.perform(MockMvcRequestBuilders.get("/hello").header("Authorization", token))
                .andDo(print())
                .andExpect(status().isOk());
    }
```
In this test, we call our `getToken()` method with a username, and the user we need, and we set up our
mocked `myConfig.getMessage()`, then we add `.header()` with our newly created token, and call the endpoint.

## Getting user information from the SecurityContext
With security setup this way, we can obtain our user's information from the Security context with the
following code snipit.

```java
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        Object o = authentication.getDetails();
        System.out.println(o);
        Object p = authentication.getPrincipal();
```

Note that this information codes from the Token only.  In order to get UserProfile data, you would
have to make a request to your user profile api.  This is a fairly standard request, depending on
what you need.

### Getting user information from the token
Spring Security can populate a UserPrincipal object that can be retrieved in the endpoint
arguments like the following...

```java
@PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/user")
    public JwtUser getUser(@AuthenticationPrincipal JwtUser principal){
        return principal;
    }
```

In order to make this happen, you need to populate a user object in your JwtTokenAuthenticationFilter after
you confirm that the token is valid.  I have implemented this in this branch's filter, using a new class
named JwtUser.  This implementation begins at line 60 of JwtTokenAuthenticationFilter.java