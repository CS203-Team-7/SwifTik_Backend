package com.swiftyticket.dto.auth;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

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
    private String email;
    private String password;
    private Date dateOfBirth;
    private long phoneNumber;
}
