package com.galvanize.util;

import com.galvanize.security.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Properties;

public class UserHelper {

    @Value("${security.jwt.secret}")
    private static String JWT_SECRET;

    JwtProperties jwtProperties;

    public static void getTokenDetails(String token){
        Claims claims = Jwts.parser()
                .setSigningKey(JWT_SECRET.getBytes())
                .parseClaimsJws(token)
                .getBody();
        Properties userProps = new Properties();
        userProps.put("username", claims.getSubject());
        userProps.put("guid", claims.get("guid", String.class));
    }
}
