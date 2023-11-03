package com.swiftyticket.services.implementations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.swiftyticket.exceptions.DuplicateUserException;
import com.swiftyticket.exceptions.UserNotFoundException;
import com.swiftyticket.models.Role;
import com.swiftyticket.models.User;
import com.swiftyticket.repositories.UserRepository;

import java.time.LocalDate;
import java.time.ZoneOffset;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {UserServiceImpl.class})
@ExtendWith(SpringExtension.class)
class UserServiceUnitTests {
    @MockBean
    private UserRepository userRepository;

    @Autowired
    private UserServiceImpl userServiceImpl;

    // Helper method to get a User object
    private static User getUserInfo() {
        User newUserInfo = new User();
        newUserInfo.setDateOfBirth(new Date());
        newUserInfo.setEmail("test@gmail.com");
        newUserInfo.setPassword("Iloveyou1!");
        newUserInfo.setPhoneNumber("1234567890");
        newUserInfo.setRole(Role.USER);
        newUserInfo.setVerified(true);
        return newUserInfo;
    }

    // getAllUsers tests
    @Test
    void getAllUsers_ReturnEmptyList_ThrowsException() {
        // Act and Arrange
        when(userRepository.findAll()).thenReturn(new ArrayList<>());

        // Assert
        assertThrows(UserNotFoundException.class, () -> userServiceImpl.getAllUsers());
        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_ReturnList_Successful() {
        // Arrange
        User user = new User();
        user.setDateOfBirth(new Date());
        user.setEmail("test@gmail.com");
        user.setPassword("Iloveyou1!");
        user.setPhoneNumber("1234567890");
        user.setRole(Role.USER);
        user.setVerified(true);

        // Act
        ArrayList<User> userList = new ArrayList<>();
        userList.add(user);
        when(userRepository.findAll()).thenReturn(userList);
        List<User> actualAllUsers = userServiceImpl.getAllUsers();

        // Assert
        verify(userRepository).findAll();
        assertEquals(1, actualAllUsers.size());
        assertSame(userList, actualAllUsers);
    }

    @Test
    void getAllUsers_UserNotFound_ThrowsException() {
        // Arrange and Act
        when(userRepository.findAll()).thenThrow(new UserNotFoundException());

        // Assert
        assertThrows(UserNotFoundException.class, () -> userServiceImpl.getAllUsers());
        verify(userRepository).findAll();
    }

    // getUserByEmail tests
    @Test
    void getUserByEmail_ValidEmail_Successful() {
        // Arrange
        User user = new User();
        user.setDateOfBirth(new Date());
        user.setEmail("test@gmail.com");
        user.setPassword("Iloveyou1!");
        user.setPhoneNumber("1234567890");
        user.setRole(Role.USER);
        user.setVerified(true);
        Optional<User> ofResult = Optional.of(user);

        // Act
        when(userRepository.findByEmail(Mockito.<String>any())).thenReturn(ofResult);
        User actualUserByEmail = userServiceImpl.getUserByEmail("test@gmail.com");

        // Assert
        verify(userRepository).findByEmail(Mockito.<String>any());
        assertSame(user, actualUserByEmail);
    }

    @Test
    void getUserByEmail_NullEmail_ThrowsException() {
        // Arrange
        Optional<User> emptyResult = Optional.empty();

        // Act
        when(userRepository.findByEmail(Mockito.<String>any())).thenReturn(emptyResult);

        // Assert
        assertThrows(UserNotFoundException.class, () -> userServiceImpl.getUserByEmail("test@gmail.com"));
        verify(userRepository).findByEmail(Mockito.<String>any());
    }

    @Test
    void getUserByEmail_UserNotFound_ThrowsException() {
        // Arrange and Act
        when(userRepository.findByEmail(Mockito.<String>any())).thenThrow(new UserNotFoundException());

        // Assert
        assertThrows(UserNotFoundException.class, () -> userServiceImpl.getUserByEmail("test@gmail.com"));
        verify(userRepository).findByEmail(Mockito.<String>any());
    }


    // updateUser tests

    @Test
    void updateUser_EmailAlreadyExists_ThrowException() {
        User user = getUserInfo();
        Optional<User> ofResult = Optional.of(user);
        when(userRepository.findByEmail(Mockito.<String>any())).thenReturn(ofResult);

        User newUserInfo = getUserInfo();
        assertThrows(DuplicateUserException.class, () -> userServiceImpl.updateUser("test@gmail.com", newUserInfo));
        verify(userRepository).findByEmail(Mockito.<String>any());
    }

    /**
     * Method under test: {@link UserServiceImpl#updateUser(String, User)}
     */
    @Test
    void updateUser_EmailDoesNotExist_ThrowException() {
        when(userRepository.findByEmail(Mockito.<String>any())).thenThrow(new UserNotFoundException());

        User newUserInfo = getUserInfo();
        assertThrows(UserNotFoundException.class, () -> userServiceImpl.updateUser("test@gmail.com", newUserInfo));
        verify(userRepository).findByEmail(Mockito.<String>any());
    }


    // deleteUser tests
    @Test
    void deleteUser_Successful() {
        // Arrange and Act
        doNothing().when(userRepository).deleteByEmail(Mockito.<String>any());
        userServiceImpl.deleteUser("test@gmail.com");

        // Assert
        verify(userRepository).deleteByEmail(Mockito.<String>any());
    }

    @Test
    void deleteUser_UserNotFound_ThrowException() {
        // Arrange and Act
        doThrow(new UserNotFoundException()).when(userRepository).deleteByEmail(Mockito.<String>any());

        // Assert
        assertThrows(UserNotFoundException.class, () -> userServiceImpl.deleteUser("test@gmail.com"));
        verify(userRepository).deleteByEmail(Mockito.<String>any());
    }
}

