package com.authentication.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.authentication.model.User;
import com.authentication.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        System.out.println(">>> Login request received for: " + identifier);

        return repository.findByEmailOrMobile(identifier.trim(), identifier.trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email or mobile: " + identifier));
    }
}