package com.swiftyticket.services;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.swiftyticket.models.User;

public interface UserService {
    List<User> listUsers();
    User getUserByEmail(String email);
    User addUser(User user);
    User updateUser(String email, User user);
    void deleteUser(String email);
    UserDetailsService userDetailsService();
}
