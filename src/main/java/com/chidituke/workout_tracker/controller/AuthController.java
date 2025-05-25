package com.chidituke.workout_tracker.controller;

import com.chidituke.workout_tracker.dto.JwtResponse;
import com.chidituke.workout_tracker.dto.LoginRequest;
import com.chidituke.workout_tracker.dto.RegisterRequest;
import com.chidituke.workout_tracker.model.User;
import com.chidituke.workout_tracker.security.JwtTokenProvider;
import com.chidituke.workout_tracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserService userService;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody @Valid LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateJwtToken(authentication);

        return ResponseEntity.ok(new JwtResponse(jwt, loginRequest.getUsername(), ""));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid RegisterRequest registerRequest) {
        if(userService.existsByUsername(registerRequest.getUsername())){
            return ResponseEntity.badRequest()
                    .body("Error: Username already taken!");
        }

        if(userService.existsByEmail(registerRequest.getEmail())){
            return ResponseEntity.badRequest()
                    .body("Error: Email already in use!");
        }

        // Create new user account
        User user = userService.createUser(registerRequest);

        return ResponseEntity.ok("User registered successfully!");
    }

    @GetMapping("/test")
    public String testAuth() {
        return "Auth controller is working!";
    }
}
