package com.app.auth.security.jwt;

import com.app.auth.domain.ApiCredentials;
import com.app.auth.domain.ApiCredentialsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;

@Service

@Slf4j
@RequiredArgsConstructor
public class JwtAuthDetailsService implements UserDetailsService {

    private final ApiCredentialsRepository apiCredentialsRepository;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        ApiCredentials credit =  apiCredentialsRepository.findByLogin(s)
                .orElseThrow(() -> new EntityNotFoundException("Credentials not found."));
        return JwtUserFactory.create(credit);
    }

}