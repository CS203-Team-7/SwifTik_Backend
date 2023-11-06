package com.swiftyticket.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_user")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "date_of_birth")
    private Date dateOfBirth;

    @Column(name = "phone_number")
    private String phoneNumber;
    // Since we need to assign certain roles to the users that login, we use ENUMS for ease of access:
    @Enumerated(EnumType.STRING)
    private Role role;
    //lock users until they verify with OTP (set it to false)
    private boolean verified = false;


    @ManyToMany(mappedBy = "preRegisteredUsers4Zone", fetch = FetchType.EAGER)
    private List<Zones> preRegisteredZones = new ArrayList<>();


    @ManyToMany(mappedBy = "preRegisteredUsers4Event", fetch = FetchType.EAGER)
    private List<Event> preRegisteredEvents = new ArrayList<>();


    @ManyToMany(mappedBy = "winnerList", fetch = FetchType.EAGER)
    private List<Zones> zonesWon = new ArrayList<>();


    @OneToMany(mappedBy = "forUser",
               cascade = CascadeType.ALL,
               fetch = FetchType.EAGER)
    private List<Ticket> ticketsBought = new ArrayList<>();

    //List<Integer> ticketsBought;

    // Below are all the methods that need to be implemented for Spring Security to actually be able to authorize this User:

    @JsonIgnore
    public User (String email, String password, Date dateOfBirth, String phoneNumber, Role role, boolean verified){
        this.email = email;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.verified = verified;

        this.preRegisteredZones = new ArrayList<>();;
        this.preRegisteredEvents = new ArrayList<>();
        this.ticketsBought = new ArrayList<>();

        this.zonesWon = new ArrayList<>();

    }

    @JsonIgnore
    @Override
    public String getUsername() {
        return email;
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    // For now I've set these to true but later we can add the necessary business logic if need be:
    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return verified;
    }
    
    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean equals(Object other){
        if( !(other instanceof User) ){
            return false;
        }

        return this.userId == ((User)other).userId;
    }
}
