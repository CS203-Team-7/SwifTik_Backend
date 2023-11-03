package com.swiftyticket.services.implementations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;

import com.swiftyticket.dto.auth.AuthResponse;
import com.swiftyticket.dto.auth.CustomUserDTO;
import com.swiftyticket.dto.auth.SignInRequest;
import com.swiftyticket.dto.auth.SignUpRequest;
import com.swiftyticket.dto.otp.OtpRequest;
import com.swiftyticket.dto.otp.OtpResponseDto;
import com.swiftyticket.dto.otp.OtpStatus;
import com.swiftyticket.exceptions.AccountNotVerifiedException;
import com.swiftyticket.exceptions.IncorrectUserPasswordException;
import com.swiftyticket.models.Role;
import com.swiftyticket.models.User;
import com.swiftyticket.repositories.UserRepository;
import com.swiftyticket.services.JwtService;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {AuthServiceImpl.class})
@ExtendWith(SpringExtension.class)
class AuthServiceUnitTests {
    @Autowired
    private AuthServiceImpl authServiceImpl;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private SmsServiceImpl smsServiceImpl;

    @MockBean
    private UserRepository userRepository;

    private final User user = new User();
    @BeforeEach
    void setUp() {
        user.setDateOfBirth(new Date());
        user.setEmail("test@gmail.com");
        user.setPassword("Iloveyou1!");
        user.setPhoneNumber("1234567890");
        user.setRole(Role.USER);
        user.setVerified(true);
    }

    // Sign up tests:
    @Test
    void signUp_ValidCredentials_Success() {
        // Arrange
        when(userRepository.save(Mockito.<User>any())).thenReturn(user);
        when(smsServiceImpl.sendSMS(Mockito.<OtpRequest>any()))
                .thenReturn(new OtpResponseDto(OtpStatus.DELIVERED, "OTP code: 123456"));
        when(passwordEncoder.encode(Mockito.<CharSequence>any())).thenReturn("secret");

        // Act
        String actualSignupResult = authServiceImpl.signup(new SignUpRequest());

        // Assert
        verify(smsServiceImpl).sendSMS(Mockito.<OtpRequest>any());
        verify(userRepository).save(Mockito.<User>any());
        verify(passwordEncoder).encode(Mockito.<CharSequence>any());
        assertEquals("Sign up successful, please check your phone for the OTP code.", actualSignupResult);
    }

    @Test
    void signUp_InvalidCredentials_ThrowsAccountNotVerified() {
        // Arrange
        when(passwordEncoder.encode(Mockito.<CharSequence>any()))
                .thenThrow(new AccountNotVerifiedException());

        // Act and Assert
        assertThrows(AccountNotVerifiedException.class, () -> authServiceImpl.signup(new SignUpRequest()));
        verify(passwordEncoder).encode(Mockito.<CharSequence>any());
    }

    @Test
    void signUp_UnverifiedUser_ThrowsAccountNotVerified() {
        // Arrange
        when(userRepository.save(Mockito.<User>any())).thenReturn(user);
        when(smsServiceImpl.sendSMS(Mockito.<OtpRequest>any()))
                .thenThrow(new AccountNotVerifiedException());
        when(passwordEncoder.encode(Mockito.<CharSequence>any())).thenReturn("secret");

        // Act
        SignUpRequest.SignUpRequestBuilder builderResult = SignUpRequest.builder();

        // Assert
        assertThrows(AccountNotVerifiedException.class,
                () -> authServiceImpl.signup(builderResult
                        .dateOfBirth(new Date())
                        .email("test@gmail.com")
                        .password("Iloveyou1!")
                        .phoneNumber("1234567890")
                        .build()));
        verify(smsServiceImpl).sendSMS(Mockito.<OtpRequest>any());
        verify(userRepository).save(Mockito.<User>any());
        verify(passwordEncoder).encode(Mockito.<CharSequence>any());
    }

    // Sign in tests:
    @Test
    void signIn_VerifiedUser_ValidCredentials_Success() throws AccountNotVerifiedException, IncorrectUserPasswordException, AuthenticationException {
        // Arrange
        Optional<User> ofResult = Optional.of(user);
        when(userRepository.findByEmail(Mockito.<String>any())).thenReturn(ofResult);
        when(jwtService.generateToken(Mockito.<User>any())).thenReturn("Bearer ABC123");
        when(authenticationManager.authenticate(Mockito.<Authentication>any()))
                .thenReturn(new TestingAuthenticationToken("Principal", "Credentials"));

        // Act
        AuthResponse actualSignInResult = authServiceImpl.signIn(new SignInRequest("test@gmail.com", "Iloveyou1!"));

        // Assert
        verify(userRepository).findByEmail(Mockito.<String>any());
        verify(jwtService).generateToken(Mockito.<User>any());
        verify(authenticationManager).authenticate(Mockito.<Authentication>any());
        CustomUserDTO customUser = actualSignInResult.getCustomUser();
        assertEquals("1234567890", customUser.getPhoneNumber());
        assertEquals("Bearer ABC123", actualSignInResult.getToken());
        assertEquals("test@gmail.com", customUser.getEmail());
        assertSame(user.getDateOfBirth(), customUser.getDateOfBirth());
    }

    @Test
    void signIn_UnVerifiedUser_ValidCredentials_ThrowsException() throws AccountNotVerifiedException, IncorrectUserPasswordException, AuthenticationException {
        // Arrange
        Optional<User> ofResult = Optional.of(user);
        when(userRepository.findByEmail(Mockito.<String>any())).thenReturn(ofResult);
        when(authenticationManager.authenticate(Mockito.<Authentication>any()))
                .thenThrow(new AccountNotVerifiedException());

        // Act and Assert
        assertThrows(IncorrectUserPasswordException.class,
                () -> authServiceImpl.signIn(new SignInRequest("test@gmail.com", "Iloveyou1!")));
        verify(userRepository).findByEmail(Mockito.<String>any());
        verify(authenticationManager).authenticate(Mockito.<Authentication>any());
    }

    @Test
    void SignIn_VerifiedUser_NullCredentials_ThrowsException() throws AccountNotVerifiedException, IncorrectUserPasswordException, AuthenticationException {
        // Arrange
        User user = mock(User.class);
        when(user.getEmail()).thenThrow(new IllegalArgumentException("Email does not exist"));
        when(user.isVerified()).thenReturn(true);

        // This is to mimic the user entering null credentials:
        doNothing().when(user).setDateOfBirth(Mockito.<Date>any());
        doNothing().when(user).setEmail(Mockito.<String>any());
        doNothing().when(user).setPassword(Mockito.<String>any());
        doNothing().when(user).setPhoneNumber(Mockito.<String>any());
        doNothing().when(user).setRole(Mockito.<Role>any());
        doNothing().when(user).setVerified(anyBoolean());

        // Then we set  null credentials:
        user.setDateOfBirth(new Date());
        user.setEmail("test@gmail.com");
        user.setPassword("Iloveyou1!");
        user.setPhoneNumber("1234567890");
        user.setRole(Role.USER);
        user.setVerified(true);
        Optional<User> ofResult = Optional.of(user);
        when(userRepository.findByEmail(Mockito.<String>any())).thenReturn(ofResult);
        when(jwtService.generateToken(Mockito.<User>any())).thenReturn("ABC123");
        when(authenticationManager.authenticate(Mockito.<Authentication>any()))
                .thenReturn(new TestingAuthenticationToken("Principal", "Credentials"));

        // Act and Assert
        assertThrows(IllegalArgumentException.class,
                () -> authServiceImpl.signIn(new SignInRequest("test@gmail.com", "Iloveyou1!")));
        verify(user).getEmail();
        verify(user).isVerified();
        // To make sure the setters are called:
        verify(user).setDateOfBirth(Mockito.<Date>any());
        verify(user).setEmail(Mockito.<String>any());
        verify(user).setPassword(Mockito.<String>any());
        verify(user).setPhoneNumber(Mockito.<String>any());
        verify(user).setRole(Mockito.<Role>any());
        verify(user).setVerified(anyBoolean());
        verify(userRepository).findByEmail(Mockito.<String>any());
        verify(jwtService).generateToken(Mockito.<User>any());
        verify(authenticationManager).authenticate(Mockito.<Authentication>any());
    }

    @Test
    void signIn_UnverifiedUser_NullCredentials_ThrowsException() throws AccountNotVerifiedException, IncorrectUserPasswordException {
        // Arrange
        User user = mock(User.class);
        when(user.isVerified()).thenReturn(false);

        // This is to mimic the user entering null credentials:
        doNothing().when(user).setDateOfBirth(Mockito.<Date>any());
        doNothing().when(user).setEmail(Mockito.<String>any());
        doNothing().when(user).setPassword(Mockito.<String>any());
        doNothing().when(user).setPhoneNumber(Mockito.<String>any());
        doNothing().when(user).setRole(Mockito.<Role>any());
        doNothing().when(user).setVerified(anyBoolean());

        // Then we set  null credentials:
        user.setDateOfBirth(new Date());
        user.setEmail("test@gmail.com");
        user.setPassword("Iloveyou1!");
        user.setPhoneNumber("1234567890");
        user.setRole(Role.USER);
        user.setVerified(true);
        Optional<User> ofResult = Optional.of(user);
        when(userRepository.findByEmail(Mockito.<String>any())).thenReturn(ofResult);

        // Act and Assert
        assertThrows(AccountNotVerifiedException.class,
                () -> authServiceImpl.signIn(new SignInRequest("test@gmail.com", "Iloveyou1!")));
        verify(user).isVerified();
        // To make sure the setters are called:
        verify(user).setDateOfBirth(Mockito.<Date>any());
        verify(user).setEmail(Mockito.<String>any());
        verify(user).setPassword(Mockito.<String>any());
        verify(user).setPhoneNumber(Mockito.<String>any());
        verify(user).setRole(Mockito.<Role>any());
        verify(user).setVerified(anyBoolean());
        verify(userRepository).findByEmail(Mockito.<String>any());
    }

    @Test
    void signIn_NoEmail_ThrowsException() throws AccountNotVerifiedException, IncorrectUserPasswordException {
        // Arrange
        Optional<User> emptyResult = Optional.empty();
        when(userRepository.findByEmail(Mockito.<String>any())).thenReturn(emptyResult);

        // Act and Assert
        assertThrows(IncorrectUserPasswordException.class,
                () -> authServiceImpl.signIn(new SignInRequest("test@gmail.com", "Iloveyou1!")));
        verify(userRepository).findByEmail(Mockito.<String>any());
    }

    @Test
    void SignIn_WrongPassword_ThrowsException() throws IncorrectUserPasswordException {
        // Arrange
        Optional<User> ofResult = Optional.of(user);
        when(userRepository.findByEmail(Mockito.<String>any())).thenReturn(ofResult);
        when(authenticationManager.authenticate(Mockito.<Authentication>any()))
                .thenThrow(new IncorrectUserPasswordException());

        // Act and Assert
        assertThrows(IncorrectUserPasswordException.class,
                () -> authServiceImpl.signIn(new SignInRequest("test@gmail.com", "wrongPassword")));
        verify(authenticationManager).authenticate(Mockito.<Authentication>any());
    }
}

