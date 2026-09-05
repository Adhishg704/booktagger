package com.nemo.booktagger.service.impl;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nemo.booktagger.entity.User;
import com.nemo.booktagger.exception.DuplicateResourceException;
import com.nemo.booktagger.exception.InvalidCredentialsException;
import com.nemo.booktagger.rest.dto.request.LoginRequest;
import com.nemo.booktagger.rest.dto.request.SignupRequest;
import com.nemo.booktagger.security.JwtService;
import com.nemo.booktagger.service.AuthService;
import com.nemo.booktagger.service.UserService;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserService userService, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public void signup(SignupRequest signupRequest) {
        String username = signupRequest.username();
        String email = signupRequest.email();
        String password = signupRequest.password();

        if (userService.isUsernameTaken(username)) {
            throw new DuplicateResourceException("Username is already taken!");
        }

        if (userService.isEmailTaken(email)) {
            throw new DuplicateResourceException("Email is already in use!");
        }

        String encryptedPassword = passwordEncoder.encode(password);
        userService.createUser(username, email, encryptedPassword);
    }

    @Override
    public String login(LoginRequest loginRequest) {
        String usernameOrEmail = loginRequest.usernameOrEmail();
        String password = loginRequest.password();

        Optional<User> user = userService.getUserByUsernameOrEmail(usernameOrEmail);

        if (user.isEmpty() || !passwordEncoder.matches(password, user.get().getPassword())) {
            throw new InvalidCredentialsException("Invalid username/email or password");
        }

        return jwtService.generateAccessToken(user.get().getId(), user.get().getUsername());
    }
}
