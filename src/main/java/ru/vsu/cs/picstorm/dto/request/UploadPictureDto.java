package ru.vsu.cs.picstorm.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.picstorm.entity.PictureType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadPictureDto {
    @NotNull(message = "Предоставьте тип фото")
    private PictureType pictureType;
    @NotNull(message = "Предоставьте данные фото")
    private MultipartFile picture;
}
