package ru.vsu.cs.picstorm.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginDto {
    @NotBlank(message = "Введите ваше имя")
    @Size(max = 20, message = "Длина вашего имени должна быть <= 20 символов")
    @Schema(name = "Username", example = "Some Name")
    private String nickname;

    @NotBlank(message = "Введите ваш пароль")
    @Size(max = 100, message = "Длина вашего пароля должна быть <= 100 символов")
    @Schema(name = "Password", example = "qwerty1234")
    private String password;
}
