package com.swiftyticket.controllers;
import java.util.List;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.swiftyticket.exceptions.UserNotFoundException;
import com.swiftyticket.models.User;
import com.swiftyticket.services.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers(){
      return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{email}")
    public ResponseEntity<User> getUserByEmail(@Nonnull @PathVariable String email){
        User user = userService.getUserByEmail(email);
        if (user == null) throw new UserNotFoundException(email);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/update/{email}")
    public ResponseEntity<User> updateUser(@Nonnull @PathVariable String email, @Nonnull @RequestBody User newUserInfo)
            throws UserNotFoundException {
        User user = userService.updateUser(email, newUserInfo);
        if (user == null) throw new UserNotFoundException(email);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{email}")
    public ResponseEntity<String> deleteUser(@Nonnull @PathVariable String email){
        userService.deleteUser(email);
        return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);
    }

}
