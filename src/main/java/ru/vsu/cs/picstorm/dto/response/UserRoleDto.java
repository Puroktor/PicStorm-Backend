package ru.vsu.cs.picstorm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.vsu.cs.picstorm.entity.UserRole;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleDto {
    private Long userId;
    private UserRole newRole;
}
