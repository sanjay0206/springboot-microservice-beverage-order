package com.infybuzz.security;

import com.infybuzz.entity.UserCredEntity;
import com.infybuzz.repository.UserCredRepository;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@ToString
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    UserCredRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserCredEntity> credential = repository.findByUsername(username);

        return credential
                .map(userCredEntity -> new CustomUserDetails(userCredEntity, List.of(userCredEntity.getRole().name())))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}
