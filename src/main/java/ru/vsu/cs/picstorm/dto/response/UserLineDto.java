package ru.vsu.cs.picstorm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLineDto {
    private Long userId;
    private byte[] avatar;
    private String nickname;
    private Boolean subscribed;
}
