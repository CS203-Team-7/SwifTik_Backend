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
import org.springframework.web.bind.annotation.RequestParam;
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
    
    //using userID, can be removed if not needed
    @GetMapping("/users/{userID}")
    public User findUser(@PathVariable Integer userID) {
        User user = userService.getUser(userID);

        if (user == null) throw new UserNotFoundException(userID);
        return user;
    }

    //using email
    //still broken, I'll debug later
    @GetMapping(value = "/users/{email}", params = "email")
    public User findUser(@RequestParam(value="email") String email) {
        User user = userService.getUserByEmail(email);

        if (user == null) throw new UserNotFoundException(email);
        return user;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/users")
    public void addUser(@RequestBody User user){
        userService.addUser(user);
    }

    //using UserID, can be removed if not needed
    @PutMapping("/users/{userID}")
    public User updateUser(@PathVariable Integer userID, @RequestBody User newUserInfo) throws UserNotFoundException{
        User user = userService.updateUser(userID, newUserInfo);
        if(user == null) throw new UserNotFoundException(userID);
        
        return user;
    }

    @PutMapping(value = "/users/{email}", params = "email")
    public User updateUser(@RequestParam(value="email") String email, @RequestBody User newUserInfo) throws UserNotFoundException{
        User user = userService.updateUser(email, newUserInfo);
        if(user == null) throw new UserNotFoundException(email);
        
        return user;
    }

    @DeleteMapping("/users/{userID}")
    public String deleteUser(@PathVariable Integer userID){
        try {
            userService.deleteUser(userID);
        } catch(EmptyResultDataAccessException e) {
            throw new UserNotFoundException(userID);
        }
        return "User "+ userID + " deleted.";
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
