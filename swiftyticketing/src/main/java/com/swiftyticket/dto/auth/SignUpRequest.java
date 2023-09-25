package com.swiftyticket.dto.auth;

import java.util.Date;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {
    // This is just to format the data that is sent when a new user is signing up in the website:
    @Nonnull
    private String email;
    @Nonnull
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[#$@!%&*?])[A-Za-z\\d#$@!%&*?]{8,}$", 
        message = "Password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one number and one special character")
    private String password;
    @Nonnull
    private Date dateOfBirth;
    @Nonnull
    private String phoneNumber;
}
