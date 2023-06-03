package ru.vsu.cs.picstorm.dto.request;

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
    private String nickname;

    @NotBlank(message = "Введите ваш пароль")
    @Size(max = 100, message = "Длина вашего пароля должна быть <= 100 символов")
    private String password;
}
