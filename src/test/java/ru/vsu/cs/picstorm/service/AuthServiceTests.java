package ru.vsu.cs.picstorm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import ru.vsu.cs.picstorm.dto.request.UserLoginDto;
import ru.vsu.cs.picstorm.dto.request.UserRegistrationDto;
import ru.vsu.cs.picstorm.dto.response.JwtTokenDto;
import ru.vsu.cs.picstorm.entity.User;
import ru.vsu.cs.picstorm.entity.UserRole;
import ru.vsu.cs.picstorm.repository.UserRepository;
import ru.vsu.cs.picstorm.security.JwtTokenProvider;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class AuthServiceTests {
    @MockBean
    private PasswordEncoder passwordEncoder;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private AuthService authService;
    private final String accessToken = "access";

    @BeforeEach
    public void prepareTest() {
        when(jwtTokenProvider.generateAccessToken(any())).thenReturn(accessToken);
    }

    @Test
    public void registerUserWithExistingName() {
        String name = "name";
        UserRegistrationDto registrationDto = new UserRegistrationDto(name, "pass", "email@email.com");
        User user = new User();
        when(userRepository.findByNickname(name)).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> authService.registerUser(registrationDto));
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    public void registerUserWithExistingEmail() {
        String email = "email@email.com";
        UserRegistrationDto registrationDto = new UserRegistrationDto("name", "pass", email);
        User user = new User();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> authService.registerUser(registrationDto));
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    public void registerValidUser() {
        String name = "name";
        String email = "email@email.com";
        String paasword = "pass";
        String encoded = "encoded";
        UserRegistrationDto registrationDto = new UserRegistrationDto(name, paasword, email);
        User expectedUser = new User(null, null, name, email, encoded, UserRole.ORDINARY, null, null);

        when(userRepository.findByNickname(name)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(paasword)).thenReturn(encoded);
        when(userRepository.save(any())).thenReturn(expectedUser);

        JwtTokenDto tokensDto = authService.registerUser(registrationDto);

        assertEquals(accessToken, tokensDto.getAccessToken());
        verify(passwordEncoder, times(1)).encode(paasword);
        verify(userRepository, times(1)).save(argThat(user -> {
            assertEquals(expectedUser.getNickname(), user.getNickname());
            assertEquals(expectedUser.getEmail(), user.getEmail());
            assertEquals(expectedUser.getPasswordHash(), user.getPasswordHash());
            assertEquals(expectedUser.getRole(), user.getRole());
            return true;
        }));
        verify(jwtTokenProvider, times(1)).generateAccessToken(expectedUser);
        verify(jwtTokenProvider, times(1)).generateAccessToken(expectedUser);
    }

    @Test
    public void loginUser() {
        String name = "name";
        String paasword = "pass";
        User user = User.builder().role(UserRole.ORDINARY).build();
        UserLoginDto loginDto = new UserLoginDto(name, paasword);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(name, paasword);
        when(userRepository.findByNickname(name)).thenReturn(Optional.of(user));

        JwtTokenDto tokensDto = authService.loginUser(loginDto);

        assertEquals(accessToken, tokensDto.getAccessToken());
        verify(authenticationManager, times(1)).authenticate(authToken);
    }

    @Test
    public void loginUserWithInvalidName() {
        String name = "name";
        String paasword = "pass";
        UserLoginDto loginDto = new UserLoginDto(name, paasword);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(name, paasword);
        when(authenticationManager.authenticate(authToken)).thenThrow(new BadCredentialsException("test"));

        assertThrows(AuthenticationException.class, () -> authService.loginUser(loginDto));
    }


    @Test
    public void loginBannedUser() {
        String name = "name";
        String paasword = "pass";
        User user = User.builder().role(UserRole.BANNED).build();
        UserLoginDto loginDto = new UserLoginDto(name, paasword);
        when(userRepository.findByNickname(name)).thenReturn(Optional.of(user));

        assertThrows(AccessDeniedException.class, () -> authService.loginUser(loginDto));
    }

    @Test
    public void refreshTokenForBannedUser() {
        String refreshToken = "name";
        String name = "pass";
        User user = User.builder().role(UserRole.BANNED).build();
        when(jwtTokenProvider.getUsernameFromJwt(refreshToken)).thenReturn(name);
        when(userRepository.findByNickname(name)).thenReturn(Optional.of(user));

        assertThrows(AccessDeniedException.class, () -> authService.refreshToken(refreshToken));
    }

    @Test
    public void refreshToken() {
        String refresh = "name";
        String name = "pass";
        User user = User.builder().role(UserRole.ORDINARY).build();
        when(jwtTokenProvider.getUsernameFromJwt(refresh)).thenReturn(name);
        when(userRepository.findByNickname(name)).thenReturn(Optional.of(user));

        JwtTokenDto tokensDto = authService.refreshToken(refresh);

        assertEquals(accessToken, tokensDto.getAccessToken());
    }

}
