package com.smartbook.service;

import com.smartbook.entity.BusinessOwner;
import com.smartbook.repository.BusinessOwnerRepository;
import com.smartbook.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final BusinessOwnerRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginService(
            BusinessOwnerRepository repository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public String login(String email, String password) {

        BusinessOwner owner = repository.findByEmail(email)
                .orElseThrow(() ->
                        new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(password, owner.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!owner.isEmailVerified()) {
            throw new BadCredentialsException(
                    "Please verify your email address before logging in."
            );
        }

        return jwtService.generateToken(owner.getEmail());
    }
}