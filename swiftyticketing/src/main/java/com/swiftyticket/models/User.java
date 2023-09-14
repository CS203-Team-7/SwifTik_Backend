package com.swiftyticket.models;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_user")
// We implement UserDetails from Spring Security to match all the requirements we need for authenticatons purposes
public class User implements UserDetails { 

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id")
    private Integer userId;
    // We should set this to only unique values to avoid duplicate email accounts being formed:
    @Column(unique = true)
    private String email;
    private String password;
    private Date dateOfBirth;
    private String phoneNumber;
    private String password;
    // Since we need to assign certain roles to the users that login, we use ENUMS for ease of access:
    @Enumerated(EnumType.STRING)
    private Role role;
    //lock users until they verify with OTP
    private boolean verified;

    // Below are all the methods that need to be implemented for Spring Security to actually be able to authorize this User:

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    // For now I've set these to true but later we can add the necessary business logic if need be:
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return verified;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
