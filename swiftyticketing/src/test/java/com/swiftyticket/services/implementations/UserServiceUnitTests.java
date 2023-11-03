package com.swiftyticket.services.implementations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        user.setDateOfBirth(Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
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
        user.setDateOfBirth(Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
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
    void updateUser_ValidUser_Successful() {
        // Arrange
        User user = new User();
        user.setDateOfBirth(new Date());
        user.setEmail("test@gmail.com");
        user.setPassword("Iloveyou1!");
        user.setPhoneNumber("1234567890");
        user.setRole(Role.USER);
        user.setVerified(true);
        Optional<User> ofResult = Optional.of(user);

        User user2 = new User();
        user2.setDateOfBirth(new Date());
        user2.setEmail("test@gmail.com");
        user2.setPassword("Iloveyou1!");
        user2.setPhoneNumber("1234567890");
        user2.setRole(Role.USER);
        user2.setVerified(true);

        // Act
        when(userRepository.save(Mockito.<User>any())).thenReturn(user2);
        when(userRepository.findByEmail(Mockito.<String>any())).thenReturn(ofResult);
        User newUserInfo = new User();
        newUserInfo.setDateOfBirth(new Date());
        newUserInfo.setEmail("test@gmail.com");
        newUserInfo.setPassword("Iloveyou1!");
        newUserInfo.setPhoneNumber("1234567890");
        newUserInfo.setRole(Role.USER);
        newUserInfo.setVerified(true);
        User actualUpdateUserResult = userServiceImpl.updateUser("test@gmail.com", newUserInfo);

        // Assert
        verify(userRepository).findByEmail(Mockito.<String>any());
        verify(userRepository).save(Mockito.<User>any());
        assertSame(user2, actualUpdateUserResult);
    }

    @Test
    void updateUser_InvalidUser_ThrowException() {
        // Arrange
        User user = new User();
        user.setDateOfBirth(new Date());
        user.setEmail("test@gmail.com");
        user.setPassword("IloveYou1!");
        user.setPhoneNumber("1234567890");
        user.setRole(Role.USER);
        user.setVerified(true);
        Optional<User> ofResult = Optional.of(user);

        // Act
        when(userRepository.save(Mockito.<User>any())).thenThrow(new UserNotFoundException());
        when(userRepository.findByEmail(Mockito.<String>any())).thenReturn(ofResult);

        User newUserInfo = new User();
        newUserInfo.setDateOfBirth(new Date());
        newUserInfo.setEmail("test@gmail.com");
        newUserInfo.setPassword("IloveYou1!");
        newUserInfo.setPhoneNumber("1234567890");
        newUserInfo.setRole(Role.USER);
        newUserInfo.setVerified(true);

        // Assert
        assertThrows(UserNotFoundException.class, () -> userServiceImpl.updateUser("test@gmail.com", newUserInfo));
        verify(userRepository).findByEmail(Mockito.<String>any());
        verify(userRepository).save(Mockito.<User>any());
    }

    @Test
    void updateUser_NoSuchUser_ThrowException() {
        // Arrange
        Optional<User> emptyResult = Optional.empty();

        // Act
        when(userRepository.findByEmail(Mockito.<String>any())).thenReturn(emptyResult);
        User newUserInfo = new User();
        newUserInfo.setDateOfBirth(new Date());
        newUserInfo.setEmail("test@gmail.com");
        newUserInfo.setPassword("Iloveyou1!");
        newUserInfo.setPhoneNumber("1234567890");
        newUserInfo.setRole(Role.USER);
        newUserInfo.setVerified(true);

        // Assert
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

