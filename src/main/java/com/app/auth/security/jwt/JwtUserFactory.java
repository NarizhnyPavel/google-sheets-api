package com.app.auth.security.jwt;

import com.app.auth.domain.ApiCredentials;
import lombok.Data;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Data
public final class JwtUserFactory  {

    public  static  JwtUser create (ApiCredentials user){
        return new JwtUser(
                user.getId(),
                user.getLogin(),
                user.getPassword(),
                new SimpleGrantedAuthority("api")
        );
    }

}
