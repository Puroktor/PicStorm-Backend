package ru.vsu.cs.picstorm.controller;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import ru.vsu.cs.picstorm.dto.response.PageDto;
import ru.vsu.cs.picstorm.dto.response.UserLineDto;
import ru.vsu.cs.picstorm.dto.response.UserProfileDto;
import ru.vsu.cs.picstorm.dto.response.UserRoleDto;
import ru.vsu.cs.picstorm.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserControllerTests {
    @MockBean
    private Authentication authentication;
    @MockBean
    private UserService userService;
    @Autowired
    private UserController userController;
    
    private static final String MOCK_USERNAME = "username";
    
    @BeforeEach
    public void prepareTest() {
        when(authentication.getName()).thenReturn(MOCK_USERNAME);
    }

    @Test
    public void uploadAvatarAsAuthorized() {
        byte[] picture = new byte[] {0};
        assertThrows(AuthenticationException.class, () -> userController.uploadAvatar(picture, authentication));
        verify(userService, times(0)).uploadAvatar(anyString(), any());
    }

    @Test
    @WithMockUser(authorities = "UPLOAD_AUTHORITY")
    public void uploadEmptyAvatar() {
        assertThrows(ValidationException.class, () -> userController.uploadAvatar(null, authentication));
        verify(userService, times(0)).uploadAvatar(anyString(), any());
    }

    @Test
    @WithMockUser(authorities = "UPLOAD_AUTHORITY")
    public void uploadTooBigAvatar() {
        byte[] picture = new byte[UserController.MAX_AVATAR_PICTURE_SIZE + 1];
        assertThrows(ValidationException.class, () -> userController.uploadAvatar(picture, authentication));
        verify(userService, times(0)).uploadAvatar(anyString(), any());
    }

    @Test
    @WithMockUser(authorities = "UPLOAD_AUTHORITY")
    public void uploadValidAvatar() {
        byte[] picture = new byte[] {0};
        ResponseEntity<?> returned = userController.uploadAvatar(picture, authentication);

        assertEquals(HttpStatus.CREATED, returned.getStatusCode());
        verify(userService, times(1)).uploadAvatar(MOCK_USERNAME, picture);
    }

    @Test
    public void findUsersWithValidParams() {
        int index = 0, size = 1;
        PageDto<UserLineDto> pageDto = new PageDto<>();
        when(userService.findUsersByNickname(MOCK_USERNAME, MOCK_USERNAME, index, size)).thenReturn(pageDto);
        ResponseEntity<?> returned = userController.findUsersByNickname(MOCK_USERNAME, index, size, authentication);

        assertEquals(HttpStatus.OK, returned.getStatusCode());
        assertEquals(pageDto, returned.getBody());
        verify(userService, times(1)).findUsersByNickname(MOCK_USERNAME, MOCK_USERNAME, index, size);
    }

    @Test
    public void findUsersWithoutName() {
        int index = 0, size = 1;
        PageDto<UserLineDto> pageDto = new PageDto<>();
        when(userService.findUsersByNickname(MOCK_USERNAME, null, index, size)).thenReturn(pageDto);
        ResponseEntity<?> returned = userController.findUsersByNickname(null, index, size, authentication);

        assertEquals(HttpStatus.OK, returned.getStatusCode());
        assertEquals(pageDto, returned.getBody());
        verify(userService, times(1)).findUsersByNickname(MOCK_USERNAME, null, index, size);
    }

    @Test
    public void findUsersWithInvalidValidIndex() {
        int index = -1, size = 1;
        assertThrows(ValidationException.class, () -> userController.findUsersByNickname(null, index, size, authentication));
        verify(userService, times(0)).findUsersByNickname(anyString(), anyString(), anyInt(), anyInt());
    }

    @Test
    public void findUsersWithInvalidValidSize() {
        int index = 1, size = 0;
        assertThrows(ValidationException.class, () -> userController.findUsersByNickname(null, index, size, authentication));
        verify(userService, times(0)).findUsersByNickname(anyString(), anyString(), anyInt(), anyInt());
    }

    @Test
    public void getUserProfileWithValidParams() {
        long userId = 0;
        UserProfileDto profileDto = new UserProfileDto();
        when(userService.getUserProfile(MOCK_USERNAME, userId)).thenReturn(profileDto);
        ResponseEntity<?> returned = userController.getUserProfile(userId, authentication);

        assertEquals(HttpStatus.OK, returned.getStatusCode());
        assertEquals(profileDto, returned.getBody());
        verify(userService, times(1)).getUserProfile(MOCK_USERNAME, userId);
    }

    @Test
    @WithMockUser(authorities = "BAN_USER_AUTHORITY")
    public void banUserWithNecessaryAuthority() {
        long userId = 0;
        UserRoleDto userRoleDto = new UserRoleDto();
        when(userService.banUser(MOCK_USERNAME, userId)).thenReturn(userRoleDto);
        ResponseEntity<?> returned = userController.banUser(userId, authentication);

        assertEquals(HttpStatus.OK, returned.getStatusCode());
        assertEquals(userRoleDto, returned.getBody());
        verify(userService, times(1)).banUser(MOCK_USERNAME, userId);
    }

    @Test
    public void banUserWithoutAuthority() {
        assertThrows(AuthenticationException.class, () -> userController.banUser(0, authentication));
        verify(userService, times(0)).banUser(anyString(), anyLong());
    }

    @Test
    @WithMockUser(authorities = "MANAGE_ADMINS_AUTHORITY")
    public void changeAdminRoleWithNecessaryAuthority() {
        long userId = 0;
        UserRoleDto userRoleDto = new UserRoleDto();
        when(userService.changeAdminRole(MOCK_USERNAME, userId)).thenReturn(userRoleDto);
        ResponseEntity<?> returned = userController.changeAdminRole(userId, authentication);

        assertEquals(HttpStatus.OK, returned.getStatusCode());
        assertEquals(userRoleDto, returned.getBody());
        verify(userService, times(1)).changeAdminRole(MOCK_USERNAME, userId);
    }

    @Test
    public void changeAdminRoleWithoutAuthority() {
        assertThrows(AuthenticationException.class, () -> userController.banUser(0, authentication));
        verify(userService, times(0)).changeAdminRole(anyString(), anyLong());
    }
}
