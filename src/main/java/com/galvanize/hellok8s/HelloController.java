package com.galvanize.hellok8s;

import org.springframework.beans.factory.annotation.Value;
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

    @GetMapping("/hello")
    public String sayHello(){
        return myConfig.getMessage();
    }

    @GetMapping("/secret")
    public String getSecretMessage(){
        return secretMessage;
    }

}
