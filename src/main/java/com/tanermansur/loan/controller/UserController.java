package com.tanermansur.loan.controller;

import com.tanermansur.loan.dto.AuthRequestDTO;
import com.tanermansur.loan.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@ConditionalOnProperty(prefix = "jwt", name = "secret", matchIfMissing = true)
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/generateToken")
    public String authenticateAndGetToken(@RequestBody AuthRequestDTO authRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );
        if (!authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("Invalid user request!");
        }

        String username;
        if (authRequest.getUsername().startsWith("admin.")) {
            username = authRequest.getUsername().substring(6);
        } else {
            username = authRequest.getUsername();
        }

        return jwtService.generateToken(username);
    }
}