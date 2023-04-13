package ru.vsu.cs.picstorm.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.vsu.cs.picstorm.dto.response.JwtTokensDto;
import ru.vsu.cs.picstorm.dto.request.UserLoginDto;
import ru.vsu.cs.picstorm.dto.request.UserRegistrationDto;
import ru.vsu.cs.picstorm.entity.User;
import ru.vsu.cs.picstorm.entity.UserRole;
import ru.vsu.cs.picstorm.repository.UserRepository;
import ru.vsu.cs.picstorm.security.JwtTokenProvider;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final ModelMapper modelMapper;

    public JwtTokensDto registerUser(UserRegistrationDto userDto) {
        if (userRepository.findByNickname(userDto.getNickname()).isPresent()) {
            throw new IllegalArgumentException("Пользователь с таким именем не существует");
        }
        User user = modelMapper.map(userDto, User.class);
        user.setPasswordHash(passwordEncoder.encode(userDto.getPassword()));
        user.setRole(UserRole.ORDINARY);
        user = userRepository.save(user);
        return createTokensForUser(user);
    }

    public JwtTokensDto loginUser(UserLoginDto userDto) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                userDto.getNickname(), userDto.getPassword()));
        User dbUser = userRepository.findByNickname(userDto.getNickname())
                .orElseThrow(() -> new NoSuchElementException("Пользователь не существует"));

        return createTokensForUser(dbUser);
    }

    public JwtTokensDto refreshToken(String refreshToken) {
        String nickname = tokenProvider.getUsernameFromJwt(refreshToken);
        User dbUser = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не существует"));
        return createTokensForUser(dbUser);
    }

    private JwtTokensDto createTokensForUser(User user) {
        if (user.getRole().equals(UserRole.BANNED)) {
            throw new AccessDeniedException("Пользователь заблокирован");
        }

        return new JwtTokensDto(tokenProvider.generateAccessToken(user), tokenProvider.generateRefreshToken(user));
    }
}
