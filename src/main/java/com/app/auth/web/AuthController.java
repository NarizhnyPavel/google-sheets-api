package com.app.auth.web;

import com.app.auth.model.AuthRequest;
import com.app.auth.model.AuthResponse;
import com.app.auth.model.TokenResponse;
import com.app.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final Environment environment;

    @PostMapping(value = "/auth", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<AuthResponse> register(@RequestBody AuthRequest authRequest){
        return authService.authenticate(authRequest);
    }

    @PostMapping(value = "/token/refresh", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<TokenResponse> getRefreshToken(@RequestBody AuthRequest authRequest) {
        return authService.getRefreshToken(authRequest);
    }

}
