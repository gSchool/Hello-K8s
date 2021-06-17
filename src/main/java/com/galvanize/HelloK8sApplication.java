package com.galvanize;

import com.galvanize.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class HelloK8sApplication {

    public static void main(String[] args) {
        SpringApplication.run(HelloK8sApplication.class, args);
    }

    @Bean
    public JwtProperties getJwtProperties(){
        return new JwtProperties();
    }

}
