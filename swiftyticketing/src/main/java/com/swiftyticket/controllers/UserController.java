package com.swiftyticket.controllers;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.swiftyticket.exceptions.UserNotFoundException;
import com.swiftyticket.models.User;
import com.swiftyticket.services.UserService;

@RestController
public class UserController {
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public List<User> getUsers() {
        return userService.listUsers();
    }

    @GetMapping("/users/{email}")
    public User findUser(@PathVariable String email) {
        User user = userService.getUserByEmail(email);

        if (user == null) throw new UserNotFoundException(email);
        return user;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/users")
    public void addUser(@RequestBody User user){
        userService.addUser(user);
    }

    @PutMapping("/users/{email}")
    public User updateUser(@PathVariable String email, @RequestBody User newUserInfo) throws UserNotFoundException{
        User user = userService.updateUser(email, newUserInfo);
        if(user == null) throw new UserNotFoundException(email);
        
        return user;
    }

    @DeleteMapping("/users/{email}")
    public String deleteUser(@PathVariable String email){
        try {
            userService.deleteUser(email);
        } catch(EmptyResultDataAccessException e) {
            throw new UserNotFoundException(email);
        }
        return "The account for "+ email + " has been deleted.";
    }


        // @GetMapping("/login")
    // public String getLoginPage() {
    //     return "login_page";
    // }

    // @GetMapping("/register")
    // public String getRegisterPage() {
    //     return "register_page";
    // }

}
