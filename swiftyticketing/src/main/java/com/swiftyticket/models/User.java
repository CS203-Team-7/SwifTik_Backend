package com.swiftyticket.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_user")
@Transactional
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
    @Column(name = "date_of_birth")
    private Date dateOfBirth;
    @Column(name = "phone_number")
    private String phoneNumber;
    // Since we need to assign certain roles to the users that login, we use ENUMS for ease of access:
    @Enumerated(EnumType.STRING)
    private Role role;
    //lock users until they verify with OTP (set it to false)
    private boolean verified;

    @ManyToMany(mappedBy = "preRegisteredUsers4Zone", fetch = FetchType.EAGER)
    private List<Zones> preRegisteredZones;

    @ManyToMany(mappedBy = "preRegisteredUsers4Event", fetch = FetchType.EAGER)
    private List<Event> preRegisteredEvents;

    // Below are all the methods that need to be implemented for Spring Security to actually be able to authorize this User:

    public User (String email, String password, Date dateOfBirth, String phoneNumber, Role role, boolean verified){
        this.email = email;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.verified = verified;

        this.preRegisteredZones = new ArrayList<>();;
        this.preRegisteredEvents = new ArrayList<>();

    }

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
