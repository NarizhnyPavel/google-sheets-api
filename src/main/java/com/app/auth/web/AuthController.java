package com.app.auth.web;

import com.app.auth.model.AuthPair;
import com.app.auth.model.AuthResponse;
import com.app.auth.model.CredentialsDto;
import com.app.auth.service.AuthService;
import com.app.auth.service.RegisterService;
import com.app.exceptions.model.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RegisterService registerService;

    @Operation(
            summary = "Create auth credentials",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Values written", content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "400", description = "Requested data not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping(value = "auth", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<AuthResponse> authenticate(@RequestBody AuthPair authPair){
        return authService.authenticate(authPair);
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<AuthResponse> register(@RequestBody CredentialsDto credentialsDto){
        return registerService.register(credentialsDto);
    }

}
