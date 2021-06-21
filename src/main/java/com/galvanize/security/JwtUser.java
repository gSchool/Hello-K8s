package com.galvanize.security;

import java.util.Collection;

public class JwtUser {
    Long guid;
    String username;
    String email;
    Collection<String> authorities;;

    public JwtUser(Long guid, String username, String email,
                   Collection<String> authorities) {
        this.guid = guid;
        this.username = username;
        this.email = email;
        this.authorities = authorities;
    }

    public Long getGuid() {
        return guid;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Collection<String> getAuthorities() {
        return authorities;
    }

    @Override
    public String toString() {
        return "JwtUser{" +
                "guid=" + guid +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", authorities=" + authorities +
                '}';
    }
}
