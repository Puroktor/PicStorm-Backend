package ru.vsu.cs.picstorm.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import ru.vsu.cs.picstorm.dto.response.PageDto;
import ru.vsu.cs.picstorm.dto.response.SubscriptionDto;
import ru.vsu.cs.picstorm.dto.response.UserLineDto;
import ru.vsu.cs.picstorm.entity.*;
import ru.vsu.cs.picstorm.repository.SubscriptionRepository;
import ru.vsu.cs.picstorm.repository.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class SubscriptionServiceTests {
    @MockBean
    private PictureStorageService pictureStorageService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private SubscriptionService subscriptionService;

    @Test
    public void getSubscribersForAuthorized() throws Exception {
        String viewerNick = "nick";
        long targetId = 1L;
        User target = User.builder().id(targetId).role(UserRole.ORDINARY).build();
        Picture picture = new Picture(1L, Instant.now());
        User user = User.builder().id(2L).nickname("name").avatar(picture).build();
        User viewer = User.builder().id(3L).build();
        Subscription subscription = new Subscription(1L, user, target, Instant.now());
        Subscription viewerSubscription = new Subscription(2L, viewer, user, Instant.now());

        when(userRepository.findByNickname(viewerNick)).thenReturn(Optional.of(viewer));
        when(userRepository.findById(targetId)).thenReturn(Optional.of(target));
        when(subscriptionRepository.findPageByTarget(eq(target), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(subscription)));
        when(subscriptionRepository.findBySubscriberAndTarget(viewer, user)).thenReturn(Optional.of(viewerSubscription));

        PageDto<UserLineDto> pageDto = subscriptionService.getSubscribers(viewerNick, targetId, 0, 2);

        assertTrue(pageDto.isLast());
        assertEquals(1, pageDto.getValues().size());
        UserLineDto userDto = pageDto.getValues().get(0);
        assertEquals(user.getId(), userDto.getUserId());
        assertEquals(user.getNickname(), userDto.getNickname());
        assertTrue(userDto.getSubscribed());

        verify(pictureStorageService, times(1)).getPicture(any());
    }

    @Test
    public void getSubscribersOfBanned() {
        long targetId = 1L;
        User target = User.builder().id(targetId).role(UserRole.BANNED).build();
        when(userRepository.findById(targetId)).thenReturn(Optional.of(target));
        assertThrows(IllegalArgumentException.class, () -> subscriptionService.getSubscribers(null, targetId, 0, 2));
    }

    @Test
    public void getSubscriptionsForUnauthorized() throws Exception {
        long subscriberId = 1L;
        User subscriber = User.builder().id(subscriberId).role(UserRole.ORDINARY).build();
        Picture picture = new Picture(1L, Instant.now());
        User user = User.builder().id(2L).nickname("name").avatar(picture).build();
        Subscription subscription = new Subscription(1L, subscriber, user, Instant.now());

        when(userRepository.findById(subscriberId)).thenReturn(Optional.of(subscriber));
        when(subscriptionRepository.findPageBySubscriber(eq(subscriber), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(subscription)));

        PageDto<UserLineDto> pageDto = subscriptionService.getSubscriptions(null, subscriberId, 0, 2);

        assertTrue(pageDto.isLast());
        assertEquals(1, pageDto.getValues().size());
        UserLineDto userDto = pageDto.getValues().get(0);
        assertEquals(user.getId(), userDto.getUserId());
        assertEquals(user.getNickname(), userDto.getNickname());
        assertNull(userDto.getSubscribed());

        verify(pictureStorageService, times(1)).getPicture(any());
    }

    @Test
    public void addSubscription() {
        String nickname = "nick";
        long userId = 2;
        User user1 = User.builder().id(1L).build();
        User user2 = User.builder().id(2L).role(UserRole.ORDINARY).build();

        when(userRepository.findByNickname(nickname)).thenReturn(Optional.of(user1));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user2));
        when(subscriptionRepository.findBySubscriberAndTarget(user1, user2)).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any())).thenReturn(Subscription.builder().id(1L).build());

        SubscriptionDto subscriptionDto = subscriptionService.changeSubscription(nickname, userId);
        assertEquals(1, subscriptionDto.getId());
        verify(subscriptionRepository, times(1)).save(any());
    }

    @Test
    public void deleteSubscription() {
        String nickname = "nick";
        long userId = 2;
        User user1 = User.builder().id(1L).build();
        User user2 = User.builder().id(2L).role(UserRole.ORDINARY).build();
        Subscription subscription = new Subscription(1L, user1, user2, Instant.now());

        when(userRepository.findByNickname(nickname)).thenReturn(Optional.of(user1));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user2));
        when(subscriptionRepository.findBySubscriberAndTarget(user1, user2)).thenReturn(Optional.of(subscription));

        SubscriptionDto subscriptionDto = subscriptionService.changeSubscription(nickname, userId);
        assertEquals(new SubscriptionDto(), subscriptionDto);
        verify(subscriptionRepository, times(1)).delete(subscription);
    }

    @Test
    public void changeSubscriptionToBannedUser() {
        String nickname = "nick";
        long userId = 2;
        User user1 = User.builder().id(1L).build();
        User user2 = User.builder().id(2L).role(UserRole.BANNED).build();

        when(userRepository.findByNickname(nickname)).thenReturn(Optional.of(user1));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user2));

        assertThrows(IllegalArgumentException.class, () -> subscriptionService.changeSubscription(nickname, userId));

    }

    @Test
    public void changeSubscriptionToCurrentUser() {
        String nickname = "nick";
        long userId = 2;
        User user = User.builder().id(1L).role(UserRole.ORDINARY).build();

        when(userRepository.findByNickname(nickname)).thenReturn(Optional.of(user));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> subscriptionService.changeSubscription(nickname, userId));
    }
}
