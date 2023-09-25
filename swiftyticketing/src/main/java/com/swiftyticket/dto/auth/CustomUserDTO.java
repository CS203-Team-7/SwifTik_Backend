package com.swiftyticket.dto.auth;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomUserDTO {
    String email;
    Date dateOfBirth;
    String phoneNumber;
}
