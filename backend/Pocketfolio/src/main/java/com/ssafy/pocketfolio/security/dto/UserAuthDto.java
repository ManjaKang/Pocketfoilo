package com.ssafy.pocketfolio.security.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Log4j2
@Getter
@Setter
@ToString
public class UserAuthDto extends User implements OAuth2User {

    private String email;

    private String name;

    private String from; // lower case

    private boolean isSignUp;

    private Map<String, Object> attr;

    public UserAuthDto(String username, String password, Collection<? extends GrantedAuthority> authorities, Map<String, Object> attr, String from, boolean isSignUp) {
        this(username, password, authorities);
        this.attr = attr;
        this.from = from;
        this.isSignUp = isSignUp;
    }

    public UserAuthDto(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attr;
    }

}
