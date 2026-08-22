package com.nemo.booktagger.service;

import com.nemo.booktagger.rest.dto.request.LoginRequest;
import com.nemo.booktagger.rest.dto.request.SignupRequest;

public interface AuthService {
    void signup(SignupRequest signupRequest);

    void login(LoginRequest loginRequest);
}
