package com.galvanize.hellok8s;

import com.galvanize.security.JwtUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Value("${secret.message}")
    private String secretMessage;

    MyConfig myConfig;

    public HelloController(MyConfig myConfig) {
        this.myConfig = myConfig;
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/hello")
    public String sayHello(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        Object o = authentication.getDetails();
        System.out.println(o);
        Object p = authentication.getPrincipal();

        return myConfig.getMessage();
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/secret")
    public String getSecretMessage(){
        return secretMessage;
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/user")
    public JwtUser getUser(@AuthenticationPrincipal JwtUser principal){
        return principal;
    }

    @GetMapping("/open")
    public String getSecurityInfo(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        String authorities = authentication.getAuthorities().toString();
        return String.format("Principal = %s, Authorities = %s", currentPrincipalName, authorities);
    }

}
