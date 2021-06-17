package com.galvanize.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityCredentialsConfig extends WebSecurityConfigurerAdapter {

//    @Autowired
    private JwtProperties jwtProperties;

    public SecurityCredentialsConfig(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                // make sure we use stateless session; session won't be used to store user's state.
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // handle an authorized attempts
                .exceptionHandling().authenticationEntryPoint((req, rsp, e) -> rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                .and()
//                .addFilter(new JwtTokenAuthenticationFilter(jwtProperties))
                .addFilterBefore(new JwtTokenAuthenticationFilter(jwtProperties), UsernamePasswordAuthenticationFilter.class)
                // Add a filter to validate user credentials and add token in the response header
                // What's the authenticationManager()?
                // An object provided by WebSecurityConfigurerAdapter, used to authenticate the user passing user's credentials
                // The filter needs this auth manager to authenticate the user.
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/open/**").permitAll()
                // HEALTH is EXPOSED
                .antMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                // ADMIN ACTUATOR ENDPOINTS (ARE NOT EXPOSED)
                .antMatchers(HttpMethod.GET,"/actuator/**").hasRole("ADMIN")
                // any other requests must be authenticated
                .anyRequest().authenticated();
    }

//    @Bean
//    public JwtProperties getJwtProperties(){
//        return new JwtProperties();
//    }

}
