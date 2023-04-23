package ru.vsu.cs.picstorm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.vsu.cs.picstorm.entity.PictureType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponsePictureDto {

    private PictureType pictureType;
    private byte[] data;
}
