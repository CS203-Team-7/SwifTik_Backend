package com.swiftyticket.services.implementations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
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

    @Test
    void getAllUsers_ReturnEmptyList_ThrowsException() {
        when(userRepository.findAll()).thenReturn(new ArrayList<>());
        assertThrows(UserNotFoundException.class, () -> userServiceImpl.getAllUsers());
        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_ReturnList_Successful() {
        User user = new User();
        user.setDateOfBirth(Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        user.setEmail("jane.doe@example.org");
        user.setPassword("iloveyou");
        user.setPhoneNumber("6625550144");
        user.setPreRegisteredEvents(new ArrayList<>());
        user.setPreRegisteredZones(new ArrayList<>());
        user.setRole(Role.USER);
        user.setTicketsBought(new ArrayList<>());
        user.setUserId(1);
        user.setVerified(true);
        user.setZonesWon(new ArrayList<>());

        ArrayList<User> userList = new ArrayList<>();
        userList.add(user);
        when(userRepository.findAll()).thenReturn(userList);
        List<User> actualAllUsers = userServiceImpl.getAllUsers();
        verify(userRepository).findAll();
        assertEquals(1, actualAllUsers.size());
        assertSame(userList, actualAllUsers);
    }

    @Test
    void getAllUsers_UserNotFound_ThrowsException() {
        when(userRepository.findAll()).thenThrow(new UserNotFoundException("An error occurred"));
        assertThrows(UserNotFoundException.class, () -> userServiceImpl.getAllUsers());
        verify(userRepository).findAll();
    }

    @Test
    void getUserByEmail_ValidEmail_Successful() {
        User user = new User();
        user.setDateOfBirth(Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        user.setEmail("jane.doe@example.org");
        user.setPassword("iloveyou");
        user.setPhoneNumber("6625550144");
        user.setPreRegisteredEvents(new ArrayList<>());
        user.setPreRegisteredZones(new ArrayList<>());
        user.setRole(Role.USER);
        user.setTicketsBought(new ArrayList<>());
        user.setUserId(1);
        user.setVerified(true);
        user.setZonesWon(new ArrayList<>());
        Optional<User> ofResult = Optional.of(user);
        when(userRepository.findByEmail(Mockito.<String>any())).thenReturn(ofResult);
        User actualUserByEmail = userServiceImpl.getUserByEmail("jane.doe@example.org");
        verify(userRepository).findByEmail(Mockito.<String>any());
        assertSame(user, actualUserByEmail);
    }

    @Test
    void getUserByEmail_NullEmail_ThrowsException() {
        Optional<User> emptyResult = Optional.empty();
        when(userRepository.findByEmail(Mockito.<String>any())).thenReturn(emptyResult);
        assertThrows(UserNotFoundException.class, () -> userServiceImpl.getUserByEmail("jane.doe@example.org"));
        verify(userRepository).findByEmail(Mockito.<String>any());
    }

    @Test
    void getUserByEmail_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(Mockito.<String>any())).thenThrow(new UserNotFoundException("An error occurred"));
        assertThrows(UserNotFoundException.class, () -> userServiceImpl.getUserByEmail("jane.doe@example.org"));
        verify(userRepository).findByEmail(Mockito.<String>any());
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        user.setDateOfBirth(Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        user.setEmail("jane.doe@example.org");
        user.setPassword("iloveyou");
        user.setPhoneNumber("6625550144");
        user.setPreRegisteredEvents(new ArrayList<>());
        user.setPreRegisteredZones(new ArrayList<>());
        user.setRole(Role.USER);
        user.setTicketsBought(new ArrayList<>());
        user.setUserId(1);
        user.setVerified(true);
        user.setZonesWon(new ArrayList<>());
        Optional<User> ofResult = Optional.of(user);

        User user2 = new User();
        user2.setDateOfBirth(Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        user2.setEmail("jane.doe@example.org");
        user2.setPassword("iloveyou");
        user2.setPhoneNumber("6625550144");
        user2.setPreRegisteredEvents(new ArrayList<>());
        user2.setPreRegisteredZones(new ArrayList<>());
        user2.setRole(Role.USER);
        user2.setTicketsBought(new ArrayList<>());
        user2.setUserId(1);
        user2.setVerified(true);
        user2.setZonesWon(new ArrayList<>());
        when(userRepository.save(Mockito.<User>any())).thenReturn(user2);
        when(userRepository.findByEmail(Mockito.<String>any())).thenReturn(ofResult);

        User newUserInfo = new User();
        newUserInfo.setDateOfBirth(Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        newUserInfo.setEmail("jane.doe@example.org");
        newUserInfo.setPassword("iloveyou");
        newUserInfo.setPhoneNumber("6625550144");
        newUserInfo.setPreRegisteredEvents(new ArrayList<>());
        newUserInfo.setPreRegisteredZones(new ArrayList<>());
        newUserInfo.setRole(Role.USER);
        newUserInfo.setTicketsBought(new ArrayList<>());
        newUserInfo.setUserId(1);
        newUserInfo.setVerified(true);
        newUserInfo.setZonesWon(new ArrayList<>());
        User actualUpdateUserResult = userServiceImpl.updateUser("jane.doe@example.org", newUserInfo);
        verify(userRepository).findByEmail(Mockito.<String>any());
        verify(userRepository).save(Mockito.<User>any());
        assertSame(user2, actualUpdateUserResult);
    }

    @Test
    void testUpdateUser2() {
        User user = new User();
        user.setDateOfBirth(Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        user.setEmail("jane.doe@example.org");
        user.setPassword("iloveyou");
        user.setPhoneNumber("6625550144");
        user.setPreRegisteredEvents(new ArrayList<>());
        user.setPreRegisteredZones(new ArrayList<>());
        user.setRole(Role.USER);
        user.setTicketsBought(new ArrayList<>());
        user.setUserId(1);
        user.setVerified(true);
        user.setZonesWon(new ArrayList<>());
        Optional<User> ofResult = Optional.of(user);
        when(userRepository.save(Mockito.<User>any())).thenThrow(new UserNotFoundException("An error occurred"));
        when(userRepository.findByEmail(Mockito.<String>any())).thenReturn(ofResult);

        User newUserInfo = new User();
        newUserInfo.setDateOfBirth(Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        newUserInfo.setEmail("jane.doe@example.org");
        newUserInfo.setPassword("iloveyou");
        newUserInfo.setPhoneNumber("6625550144");
        newUserInfo.setPreRegisteredEvents(new ArrayList<>());
        newUserInfo.setPreRegisteredZones(new ArrayList<>());
        newUserInfo.setRole(Role.USER);
        newUserInfo.setTicketsBought(new ArrayList<>());
        newUserInfo.setUserId(1);
        newUserInfo.setVerified(true);
        newUserInfo.setZonesWon(new ArrayList<>());
        assertThrows(UserNotFoundException.class, () -> userServiceImpl.updateUser("jane.doe@example.org", newUserInfo));
        verify(userRepository).findByEmail(Mockito.<String>any());
        verify(userRepository).save(Mockito.<User>any());
    }

    @Test
    void testUpdateUser3() {
        Optional<User> emptyResult = Optional.empty();
        when(userRepository.findByEmail(Mockito.<String>any())).thenReturn(emptyResult);

        User newUserInfo = new User();
        newUserInfo.setDateOfBirth(Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        newUserInfo.setEmail("jane.doe@example.org");
        newUserInfo.setPassword("iloveyou");
        newUserInfo.setPhoneNumber("6625550144");
        newUserInfo.setPreRegisteredEvents(new ArrayList<>());
        newUserInfo.setPreRegisteredZones(new ArrayList<>());
        newUserInfo.setRole(Role.USER);
        newUserInfo.setTicketsBought(new ArrayList<>());
        newUserInfo.setUserId(1);
        newUserInfo.setVerified(true);
        newUserInfo.setZonesWon(new ArrayList<>());
        assertThrows(UserNotFoundException.class, () -> userServiceImpl.updateUser("jane.doe@example.org", newUserInfo));
        verify(userRepository).findByEmail(Mockito.<String>any());
    }

    @Test
    void testDeleteUser() {
        doNothing().when(userRepository).deleteByEmail(Mockito.<String>any());
        userServiceImpl.deleteUser("jane.doe@example.org");
        verify(userRepository).deleteByEmail(Mockito.<String>any());
    }

    @Test
    void testDeleteUser2() {
        doThrow(new UserNotFoundException("An error occurred")).when(userRepository).deleteByEmail(Mockito.<String>any());
        assertThrows(UserNotFoundException.class, () -> userServiceImpl.deleteUser("jane.doe@example.org"));
        verify(userRepository).deleteByEmail(Mockito.<String>any());
    }
}

