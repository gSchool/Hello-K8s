package com.galvanize.hellok8s;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HelloController.class)
@TestPropertySource(locations="classpath:application-test.properties")
public class HelloControllerTest {
    @Value("${security.jwt.secret}")
    String JWT_KEY;

    @Autowired
    MockMvc mockMvc;

    @MockBean
    MyConfig myConfig;

    @Test
    void sayHello() throws Exception {
        String token = getToken("user", Arrays.asList("ROLE_USER"));
        when(myConfig.getMessage()).thenReturn("Sample Message");
        mockMvc.perform(MockMvcRequestBuilders.get("/hello").header("Authorization", token))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void getSecretMessage() throws Exception {
        String token = getToken("admin", Arrays.asList("ROLE_ADMIN"));
//        when(myConfig.getMessage()).thenReturn("Sample Message");
        mockMvc.perform(MockMvcRequestBuilders.get("/secret").header("Authorization", token))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void getSecurityInfo() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/open"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    private String getToken(String username, List<String> roles){
        long now = System.currentTimeMillis();
        String token = Jwts.builder()
                .setHeaderParam("typ","JWT")
                .setSubject(username)
                .claim("name", username)
                .claim("guid", 99)
                // Convert to list of strings.
                // This is important because it affects the way we get them back in the Gateway.
                .claim("authorities", roles)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + 5256000 * 1000L))  // in milliseconds
                .signWith(SignatureAlgorithm.HS512, JWT_KEY.getBytes())
                .compact();

        return String.format("Bearer %s", token);
    }
}