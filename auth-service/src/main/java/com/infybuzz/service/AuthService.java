package com.infybuzz.service;

import com.infybuzz.entity.UserCredEntity;
import com.infybuzz.repository.UserCredRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {
    @Autowired
    UserCredRepository repository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    JwtService jwtService;

    public UserCredEntity saveUser(UserCredEntity credential) {
        credential.setPassword(passwordEncoder.encode(credential.getPassword()));
        credential.setCreatedAt(LocalDateTime.now());
        return repository.save(credential);
    }

    public String generateToken(String username) {
        return jwtService.generateToken(username);
    }

    public boolean validateToken(String token) {
       return jwtService.validateToken(token);
    }
}
