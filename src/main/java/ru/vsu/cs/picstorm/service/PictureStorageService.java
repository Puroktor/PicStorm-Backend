package ru.vsu.cs.picstorm.service;

import ru.vsu.cs.picstorm.entity.Picture;

/**
 * Provides access to the pictures on remote storage
 */
public interface PictureStorageService {

    /**
    * Saves picture in remote storage
    */
    void savePicture(String name, byte[] picture) throws Exception;

    /**
     * Retrieves picture from remote storage
     */
    byte[] getPicture(String name) throws Exception;

    /**
     * Deletes picture in remote storage
     */
    void deletePicture(String name) throws Exception;

    /**
     * Returns avatar picture name that will be used for storing.
     */
    String getAvatarName(Picture picture);

    /**
     * Returns publication picture name that will be used for storing.
     */
    String getPublicationName(Picture picture);
}
