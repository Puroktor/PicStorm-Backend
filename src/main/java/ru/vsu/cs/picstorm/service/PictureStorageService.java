package ru.vsu.cs.picstorm.service;

import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.picstorm.entity.Picture;

public interface PictureStorageService {
    void savePicture(String name, MultipartFile pictureFile) throws Exception;
    byte[] getPicture(String name) throws Exception;
    void deletePicture(String name) throws Exception;
    String getAvatarName(Picture picture);
    String getPublicationName(Picture picture);
}
