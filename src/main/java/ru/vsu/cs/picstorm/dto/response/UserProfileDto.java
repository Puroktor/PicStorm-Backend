package ru.vsu.cs.picstorm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.vsu.cs.picstorm.entity.UserRole;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private ResponsePictureDto avatar;
    private String nickname;
    private UserRole role;
    private Boolean subscribed;
    private Long subscriptionsCount;
    private Long subscribersCount;
}
