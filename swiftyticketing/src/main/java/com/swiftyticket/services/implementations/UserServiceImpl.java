package com.swiftyticket.services.implementations;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.swiftyticket.exceptions.UserNotFoundException;
import com.swiftyticket.models.User;
import com.swiftyticket.repositories.UserRepository;
import com.swiftyticket.services.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    // We are going to need the repository methods here:
    private final UserRepository userRepository;

    @Override
    public List<User> getAllUsers() {
        List<User> usersList = userRepository.findAll();
        if(usersList.isEmpty()) throw new UserNotFoundException("No users found");
        else return usersList;
    }

    @Override
    public User getUserByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email: " + email + " not found"));
    }

    @Override
    public User updateUser(String email, User newUserInfo) {
        return userRepository.findByEmail(email).map(user -> {
            user.setDateOfBirth(newUserInfo.getDateOfBirth());
            user.setEmail(newUserInfo.getEmail());
            user.setPassword(newUserInfo.getPassword());
            user.setPhoneNumber(newUserInfo.getPhoneNumber());
            return userRepository.save(user);
        }).orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found"));
    }

    @Override
    public void deleteUser(String email){
        userRepository.deleteByEmail(email);
    }

    // Also implement the UserDetailsService for Spring security:
    @Override
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            // So basically since our user model doesn't have a username, we are just replacing it by loading a user by their unique email instead:
            @Override
            public UserDetails loadUserByUsername(String username) {
                return userRepository.findByEmail(username)
                                        .orElseThrow(() -> new UserNotFoundException("User does not exist"));
            }
        };
    }
    
}
