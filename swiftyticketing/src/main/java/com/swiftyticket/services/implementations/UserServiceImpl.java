package com.swiftyticket.services.implementations;

import java.util.List;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUser(Integer userID) {
        return userRepository.findById(userID).map(user -> {
            return user;
        }).orElse(null);
    }

    @Override
    public User getUserByEmail(String email){
        return userRepository.findByEmail(email).map(user -> {
            return user;
        }).orElse(null);
    }

    @Override
    public User addUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Integer userID, User newUserInfo) {
        return userRepository.findById(userID).map(user -> {
            user.setAge(newUserInfo.getAge());
            user.setEmail(newUserInfo.getEmail());
            user.setPassword(newUserInfo.getPassword());
            user.setPhoneNumber(newUserInfo.getPhoneNumber());
            return userRepository.save(user);
        }).orElse(null);
    }

    @Override
    public User updateUser(String email, User newUserInfo) {
        return userRepository.findByEmail(email).map(user -> {
            user.setAge(newUserInfo.getAge());
            user.setEmail(newUserInfo.getEmail());
            user.setPassword(newUserInfo.getPassword());
            user.setPhoneNumber(newUserInfo.getPhoneNumber());
            return userRepository.save(user);
        }).orElse(null);
    }

    @Override
    public void deleteUser(Integer userID){
        userRepository.deleteById(userID);
    }

    // Also implement the UserDetailsService for Spring security:
    @Override
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            // So basically since our user model doesn't have a username, we are just replacing it by loading a user by their unique email instead:
            @Override
            public UserDetails loadUserByUsername(String username) {
                return userRepository.findByEmail(username)
                                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            }
        };
    }
    
}
