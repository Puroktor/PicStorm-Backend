package ru.vsu.cs.picstorm.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.picstorm.dto.request.UserLoginDto;
import ru.vsu.cs.picstorm.dto.request.UserRegistrationDto;
import ru.vsu.cs.picstorm.dto.response.JwtTokensDto;
import ru.vsu.cs.picstorm.service.AuthService;

@RestController
@RequestMapping("/api/auth/")
@RequiredArgsConstructor
@Validated
public class AuthController {
    private final AuthService authService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("register")
    public ResponseEntity<JwtTokensDto> registerUser(
            @RequestBody @NotNull(message = "Предоставьте информацию для регистрации") @Valid UserRegistrationDto userRegistrationDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.registerUser(userRegistrationDto));
    }

    @PostMapping("login")
    public ResponseEntity<JwtTokensDto> loginUser(
            @RequestBody @NotNull(message = "Предоставьте информацию для входа") @Valid UserLoginDto userLoginDto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authService.loginUser(userLoginDto));
    }

    @PostMapping("token/refresh")
    public ResponseEntity<JwtTokensDto> refreshToken(@RequestBody @NotBlank(message = "Предоставьте токен") String refreshToken) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authService.refreshToken(refreshToken));
    }
}
