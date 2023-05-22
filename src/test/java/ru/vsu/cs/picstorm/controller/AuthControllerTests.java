package ru.vsu.cs.picstorm.controller;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import ru.vsu.cs.picstorm.dto.request.UserLoginDto;
import ru.vsu.cs.picstorm.dto.request.UserRegistrationDto;
import ru.vsu.cs.picstorm.dto.response.JwtTokensDto;
import ru.vsu.cs.picstorm.service.AuthService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class AuthControllerTests {
    @MockBean
    private AuthService authService;
    @Autowired
    private AuthController authController;

    @Test
    public void registerWithValidInfo() {
        UserRegistrationDto registrationDto = new UserRegistrationDto("name", "pass", "email@gmail.com");
        JwtTokensDto jwtTokensDto = new JwtTokensDto("access", "refresh");
        when(authService.registerUser(registrationDto)).thenReturn(jwtTokensDto);
        ResponseEntity<JwtTokensDto> returned = authController.registerUser(registrationDto);

        assertEquals(HttpStatus.CREATED, returned.getStatusCode());
        assertEquals(jwtTokensDto, returned.getBody());
        verify(authService, times(1)).registerUser(registrationDto);
    }

    @Test
    public void registerWithoutName() {
        UserRegistrationDto registrationDto = new UserRegistrationDto(null, "pass", "email@gmail.com");
        assertThrows(ValidationException.class, () -> authController.registerUser(registrationDto));
        verify(authService, times(0)).registerUser(any());
    }

    @Test
    public void registerWithBlankPassword() {
        UserRegistrationDto registrationDto = new UserRegistrationDto("name", "", "email@gmail.com");
        assertThrows(ValidationException.class, () -> authController.registerUser(registrationDto));
        verify(authService, times(0)).registerUser(any());
    }

    @Test
    public void registerWithoutEmail() {
        UserRegistrationDto registrationDto = new UserRegistrationDto("name", "", null);
        assertThrows(ValidationException.class, () -> authController.registerUser(registrationDto));
        verify(authService, times(0)).registerUser(any());
    }

    @Test
    public void registerWithTooLongName() {
        UserRegistrationDto registrationDto = new UserRegistrationDto("a".repeat(51), "pass", "email@gmail.com");
        assertThrows(ValidationException.class, () -> authController.registerUser(registrationDto));
        verify(authService, times(0)).registerUser(any());
    }

    @Test
    public void registerWithTooLongPassword() {
        UserRegistrationDto registrationDto = new UserRegistrationDto("name", "p".repeat(101), "email@gmail.com");
        assertThrows(ValidationException.class, () -> authController.registerUser(registrationDto));
        verify(authService, times(0)).registerUser(any());
    }

    @Test
    public void registerWithTooLongEmail() {
        UserRegistrationDto registrationDto = new UserRegistrationDto("name", "pass", "e".repeat(400));
        assertThrows(ValidationException.class, () -> authController.registerUser(registrationDto));
        verify(authService, times(0)).registerUser(any());
    }

    @Test
    public void registerWithInvalidEmail() {
        UserRegistrationDto registrationDto = new UserRegistrationDto("name", "pass", "email");
        assertThrows(ValidationException.class, () -> authController.registerUser(registrationDto));
        verify(authService, times(0)).registerUser(any());
    }

    @Test
    public void loginWithValidInfo() {
        UserLoginDto loginDto = new UserLoginDto("name", "pass");
        JwtTokensDto jwtTokensDto = new JwtTokensDto("access", "refresh");
        when(authService.loginUser(loginDto)).thenReturn(jwtTokensDto);
        ResponseEntity<JwtTokensDto> returned = authController.loginUser(loginDto);

        assertEquals(HttpStatus.OK, returned.getStatusCode());
        assertEquals(jwtTokensDto, returned.getBody());
        verify(authService, times(1)).loginUser(loginDto);
    }

    @Test
    public void loginWithoutName() {
        UserLoginDto loginDto = new UserLoginDto(null, "pass");
        assertThrows(ValidationException.class, () -> authController.loginUser(loginDto));
        verify(authService, times(0)).loginUser(any());
    }

    @Test
    public void loginWithBlankPassword() {
        UserLoginDto loginDto = new UserLoginDto("name", "");
        assertThrows(ValidationException.class, () -> authController.loginUser(loginDto));
        verify(authService, times(0)).loginUser(any());
    }

    @Test
    public void loginWithTooLongName() {
        UserLoginDto loginDto = new UserLoginDto("a".repeat(51), "pass");
        assertThrows(ValidationException.class, () -> authController.loginUser(loginDto));
        verify(authService, times(0)).loginUser(any());
    }

    @Test
    public void loginWithTooLongPassword() {
        UserLoginDto loginDto = new UserLoginDto("name", "p".repeat(101));
        assertThrows(ValidationException.class, () -> authController.loginUser(loginDto));
        verify(authService, times(0)).loginUser(any());
    }

    @Test
    public void refreshValidToken() {
        String refreshToken = "refreshToken";
        JwtTokensDto jwtTokensDto = new JwtTokensDto("access", "refresh");
        when(authService.refreshToken(refreshToken)).thenReturn(jwtTokensDto);
        ResponseEntity<JwtTokensDto> returned = authController.refreshToken(refreshToken);

        assertEquals(HttpStatus.OK, returned.getStatusCode());
        assertEquals(jwtTokensDto, returned.getBody());
        verify(authService, times(1)).refreshToken(refreshToken);
    }

    @Test
    public void refreshBlankToken() {
        assertThrows(ValidationException.class, () -> authController.refreshToken(""));
        verify(authService, times(0)).refreshToken(any());
    }
}
