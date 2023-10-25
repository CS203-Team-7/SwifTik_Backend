package com.swiftyticket.services.implementations;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.swiftyticket.exceptions.UserNotFoundException;
import com.swiftyticket.models.User;
import com.swiftyticket.repositories.UserRepository;
import com.swiftyticket.services.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    // We are going to need the repository methods here:
    private final UserRepository userRepository;

    /**
     * Returns a list of all users in the DB.
     * @return List<User>
     */
    @Override
    @Transactional
    public List<User> getAllUsers() {
        List<User> usersList = userRepository.findAll();
        if(usersList.isEmpty()) throw new UserNotFoundException("No users found");
        else return usersList;
    }

    /**
     * Returns a single user based on the user email.
     * @param email -> String user email (Unique identifier)
     * @throws UserNotFoundException -> if the user email does not exist in the DB
     * @return User -> User object with the specified email
     */
    @Override
    public User getUserByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email: " + email + " not found"));
    }

    /**
     * Updates the existing user with the new user details.
     * @param email -> String user email (Unique identifier)
     * @param newUserInfo -> User object containing the new user details
     * @throws UserNotFoundException -> if the user email does not exist in the DB
     * @return -> User object with the updated details
     */
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

    /**
     * Deletes the user with the specified email.
     * @param email -> String user email (Unique identifier)
     */
    @Override
    public void deleteUser(String email){
        userRepository.deleteByEmail(email);
    }

    /**
     * This function is to satisfy the UserDetailsService interface.
     * It is used by the Spring Security framework to load the user details from the DB.
     * @throws UserNotFoundException -> if the user email does not exist in the DB
     * @return UserDetailsService -> UserDetailsService object containing the user details
     */
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
