package com.nemo.booktagger.rest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
    @NotBlank
    @Size(min = 6, max = 50)
    @Pattern(
        regexp = "[A-Za-z0-9_]+",
        message = "Username can only contain letters, numbers, and underscores"
    ) 
    String username,

    @NotBlank 
    @Email 
    String email,

    @NotBlank
    @Size(min = 8, max = 100)
    @Pattern(
        regexp=".*[A-Z].*",
        message="Password must contain at least one uppercase letter"
    )
    @Pattern(
        regexp=".*[a-z].*",
        message="Password must contain at least one lowercase letter"
    )
    @Pattern(
        regexp=".*\\d.*",
        message="Password must contain at least one digit"
    )
    @Pattern(
        regexp = ".*[^A-Za-z0-9].*",
        message = "Password must contain at least one special character"
    )
    String password
) {

}
