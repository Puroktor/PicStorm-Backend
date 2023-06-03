package ru.vsu.cs.picstorm.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import ru.vsu.cs.picstorm.dto.response.*;
import ru.vsu.cs.picstorm.entity.*;
import ru.vsu.cs.picstorm.repository.PictureRepository;
import ru.vsu.cs.picstorm.repository.PublicationRepository;
import ru.vsu.cs.picstorm.repository.SubscriptionRepository;
import ru.vsu.cs.picstorm.repository.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceTests {
    @MockBean
    private PictureStorageService pictureStorageService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private SubscriptionRepository subscriptionRepository;
    @MockBean
    private PublicationRepository publicationRepository;
    @MockBean
    private PictureRepository pictureRepository;
    @Autowired
    private UserService userService;

    @Test
    public void getUserProfile() throws Exception {
        long userId = 1;
        User profileUser = User.builder().id(userId).nickname("name").role(UserRole.ORDINARY).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(profileUser));
        when(subscriptionRepository.countBySubscriber(profileUser)).thenReturn(5L);
        when(subscriptionRepository.countByTarget(profileUser)).thenReturn(10L);

        UserProfileDto profileDto = userService.getUserProfile(null, userId);
        assertEquals(profileUser.getId(), profileDto.getId());
        assertEquals(profileUser.getNickname(), profileDto.getNickname());
        assertEquals(profileUser.getRole(), profileDto.getRole());
        assertEquals(5L, profileDto.getSubscriptionsCount());
        assertEquals(10L, profileDto.getSubscribersCount());
        assertNull(profileDto.getSubscribed());

        verify(pictureStorageService, times(0)).getPicture(any());
    }

    @Test
    public void getUserProfileOfBannedUser() {
        String nickname = "name";
        long userId = 1;
        User user1 = User.builder().id(userId).role(UserRole.BANNED).build();
        User user2 = User.builder().nickname(nickname).build();

        when(userRepository.findByNickname(nickname)).thenReturn(Optional.of(user2));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user1));

        assertThrows(IllegalArgumentException.class, () -> userService.getUserProfile(nickname, userId));
    }

    @Test
    public void findUsersByNicknameForUnauthorized() throws Exception {
        String input = "test";
        long targetId = 1L;
        User user = User.builder().id(2L).nickname("name").build();

        when(userRepository.findById(targetId)).thenReturn(Optional.of(user));
        when(userRepository.findPageByNickname(eq(input), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(user)));

        PageDto<UserLineDto> pageDto = userService.findUsersByNickname(null, input, 0, 2);

        assertTrue(pageDto.isLast());
        assertEquals(1, pageDto.getValues().size());
        UserLineDto userDto = pageDto.getValues().get(0);
        assertEquals(user.getId(), userDto.getUserId());
        assertEquals(user.getNickname(), userDto.getNickname());
        assertNull(userDto.getSubscribed());

        verify(pictureStorageService, times(0)).getPicture(any());
    }

    @Test
    public void findUsersByNicknameForAuthorized() throws Exception {
        String searcherNick = "nick";
        String input = "test";
        long targetId = 1L;
        User searcher = User.builder().id(targetId).role(UserRole.ORDINARY).build();
        Picture picture = new Picture(1L, Instant.now());
        User user = User.builder().id(2L).nickname("name").avatar(picture).build();
        Subscription subscription = new Subscription(1L, searcher, user, Instant.now());

        when(userRepository.findByNickname(searcherNick)).thenReturn(Optional.of(searcher));
        when(userRepository.findById(targetId)).thenReturn(Optional.of(user));
        when(userRepository.findPageByNickname(eq(input), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(user)));
        when(subscriptionRepository.findBySubscriberAndTarget(searcher, user)).thenReturn(Optional.of(subscription));

        PageDto<UserLineDto> pageDto = userService.findUsersByNickname(searcherNick, input, 0, 2);

        assertTrue(pageDto.isLast());
        assertEquals(1, pageDto.getValues().size());
        UserLineDto userDto = pageDto.getValues().get(0);
        assertEquals(user.getId(), userDto.getUserId());
        assertEquals(user.getNickname(), userDto.getNickname());
        assertTrue(userDto.getSubscribed());
    }

    @Test
    public void banUser() {
        String nickname = "name";
        long userId = 1;
        Publication publication = Publication.builder().state(PublicationState.VISIBLE).build();
        User user1 = User.builder().id(userId).role(UserRole.ORDINARY).publications(List.of(publication)).build();
        User user2 = User.builder().nickname(nickname).build();

        when(userRepository.findByNickname(nickname)).thenReturn(Optional.of(user2));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user1));
        UserRoleDto roleDto = userService.banUser(nickname, userId);

        assertEquals(UserRole.BANNED, roleDto.getNewRole());
        assertEquals(PublicationState.USER_BANNED, publication.getState());
        verify(userRepository, times(1)).save(user1);
        verify(publicationRepository, times(1)).save(publication);
    }

    @Test
    public void banAdmin() {
        String nickname = "name";
        long userId = 1;
        User user = User.builder().id(userId).role(UserRole.ADMIN).build();

        when(userRepository.findByNickname(nickname)).thenReturn(Optional.of(new User()));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        assertThrows(AccessDeniedException.class, () -> userService.banUser(nickname, userId));
    }

    @Test
    public void changeAdminRole() {
        String nickname = "name";
        long userId = 1;
        User user = User.builder().id(userId).role(UserRole.ORDINARY).build();

        when(userRepository.findByNickname(nickname)).thenReturn(Optional.of(new User()));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        UserRoleDto roleDto = userService.changeAdminRole(nickname, userId);

        assertEquals(UserRole.ADMIN, roleDto.getNewRole());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void uploadAvatar() throws Exception {
        String nickname = "name";
        String avatarName = "avatar";
        byte[] photo = new byte[] {0};
        User user = User.builder().id(1L).build();
        Picture picture = Picture.builder().id(1L).build();

        when(userRepository.findByNickname(nickname)).thenReturn(Optional.of(user));
        when(pictureRepository.save(any())).thenReturn(picture);
        when(pictureStorageService.getAvatarName(picture)).thenReturn(avatarName);

        userService.uploadAvatar(nickname, photo);

        verify(pictureRepository, times(1)).save(any());
        verify(pictureStorageService, times(1)).getAvatarName(picture);
        verify(pictureStorageService, times(1)).savePicture(avatarName, photo);
        verify(userRepository, times(1)).save(argThat(u -> {
            assertEquals(picture, u.getAvatar());
            return true;
        }));
    }

    @Test
    public void uploadPublicationWithExceptionDuringLoading() throws Exception {
        String nickname = "name";
        String avatarName = "avatar";
        byte[] photo = new byte[] {0};
        User user = User.builder().id(1L).build();
        Picture picture = Picture.builder().id(1L).build();

        when(userRepository.findByNickname(nickname)).thenReturn(Optional.of(user));
        when(pictureRepository.save(any())).thenReturn(picture);
        when(pictureStorageService.getAvatarName(picture)).thenReturn(avatarName);
        doThrow(Exception.class).when(pictureStorageService).savePicture(avatarName, photo);

        assertThrows(RuntimeException.class, () -> userService.uploadAvatar(nickname, photo));

        verify(pictureRepository, times(1)).save(any());
        verify(pictureRepository, times(1)).delete(picture);
        verify(pictureStorageService, times(1)).getAvatarName(picture);
        verify(pictureStorageService, times(1)).savePicture(avatarName, photo);
        verify(userRepository, times(0)).save(any());
    }

    @Test
    public void getAvatar() throws Exception {
        long userId = 1L;
        String pictureName = "picture";
        byte[] pictureData = new byte[10];
        Picture picture = new Picture();
        User user = User.builder().id(userId).avatar(picture).role(UserRole.ORDINARY).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(pictureStorageService.getAvatarName(any())).thenReturn(pictureName);
        when(pictureStorageService.getPicture(pictureName)).thenReturn(pictureData);

        PictureDto returnedPicture = userService.getUserAvatar(userId);

        assertArrayEquals(returnedPicture.getPicture(), pictureData);
        verify(pictureStorageService, times(1)).getPicture(pictureName);
    }

    @Test
    public void getAvatarOfBanned() {
        long userId = 1L;
        User user = User.builder().id(userId).role(UserRole.BANNED).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        assertThrows(AccessDeniedException.class, () -> userService.getUserAvatar(userId));
    }
}
