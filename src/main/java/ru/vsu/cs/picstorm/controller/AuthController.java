package ru.vsu.cs.picstorm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.picstorm.dto.response.JwtTokensDto;
import ru.vsu.cs.picstorm.dto.request.UserLoginDto;
import ru.vsu.cs.picstorm.dto.request.UserRegistrationDto;
import ru.vsu.cs.picstorm.service.AuthService;

@RestController
@RequestMapping("/api/auth/")
@RequiredArgsConstructor
@Validated
@Tag(name = "Authentication API", description = "Allows to  register, login and refresh JWT tokens")
public class AuthController {
    private final AuthService authService;
    @Operation(summary = "Registers new user and returns corresponding JWT tokens")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("register")
    public ResponseEntity<JwtTokensDto> registerUser(
            @RequestBody @NotNull(message = "Предоставьте информацию для регистрации") @Valid
            @Parameter(name = "Your registration info") UserRegistrationDto userRegistrationDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.registerUser(userRegistrationDto));
    }

    @Operation(summary = "Returns JWT access and refresh tokens if OK")
    @PostMapping("login")
    public ResponseEntity<JwtTokensDto> loginUser(
            @RequestBody @NotNull(message = "Предоставьте информацию для входа") @Valid
            @Parameter(name = "Your login info") UserLoginDto userLoginDto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authService.loginUser(userLoginDto));
    }

    @Operation(summary = "Returns JWT access and refresh tokens if OK")
    @PostMapping("token/refresh")
    public ResponseEntity<JwtTokensDto> refreshToken(@RequestBody @NotBlank(message = "Предоставьте токен")
                                                     @Parameter(name = "Your login info") String refreshToken) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authService.refreshToken(refreshToken));
    }
}
