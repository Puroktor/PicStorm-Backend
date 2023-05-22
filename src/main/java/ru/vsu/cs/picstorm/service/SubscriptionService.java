package ru.vsu.cs.picstorm.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ru.vsu.cs.picstorm.dto.response.PageDto;
import ru.vsu.cs.picstorm.dto.response.SubscriptionDto;
import ru.vsu.cs.picstorm.dto.response.UserLineDto;
import ru.vsu.cs.picstorm.entity.Subscription;
import ru.vsu.cs.picstorm.entity.User;
import ru.vsu.cs.picstorm.entity.UserRole;
import ru.vsu.cs.picstorm.repository.SubscriptionRepository;
import ru.vsu.cs.picstorm.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiFunction;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ModelMapper modelMapper;

    public PageDto<UserLineDto> getSubscribers(@Nullable String viewingUserNickname, long userId, int index, int size) {
        BiFunction<User, Pageable, Page<User>> findUsersFunction = (user, pageable) -> {
            Page<Subscription> subscriptions = subscriptionRepository.findPageByTarget(user, pageable);
            return subscriptions.map(Subscription::getSubscriber);
        };
        return getSubscriptionUserList(findUsersFunction, viewingUserNickname, userId, index, size);
    }

    public PageDto<UserLineDto> getSubscriptions(@Nullable String viewingUserNickname, long userId, int index, int size) {
        BiFunction<User, Pageable, Page<User>> findUsersFunction = (user, pageable) -> {
            Page<Subscription> subscriptions = subscriptionRepository.findPageBySubscriber(user, pageable);
            return subscriptions.map(Subscription::getTarget);
        };
        return getSubscriptionUserList(findUsersFunction, viewingUserNickname, userId, index, size);
    }

    private PageDto<UserLineDto> getSubscriptionUserList(BiFunction<User, Pageable, Page<User>> findUsersFunction,
                                                         String viewingUserNickname, long userId, int index, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не существует"));
        if (user.getRole().equals(UserRole.BANNED)) {
            throw new IllegalArgumentException("Пользователь заблокирован");
        }
        User viewingUser = null;
        if (viewingUserNickname !=null) {
            viewingUser = userRepository.findByNickname(viewingUserNickname)
                    .orElseThrow(() -> new NoSuchElementException("Пользователь не существует"));
        }
        Pageable pageable = PageRequest.of(index, size, Sort.by("nickname"));
        Page<User> subscriptionUsersPage = findUsersFunction.apply(user, pageable);
        List<UserLineDto> subscribersDtoList = userService.mapUserResultListForView(viewingUser, subscriptionUsersPage);
        return new PageDto<>(subscribersDtoList, index, size, subscriptionUsersPage.isLast());
    }

    public SubscriptionDto changeSubscription(String requesterUsername, long userId) {
        User requestingUser = userRepository.findByNickname(requesterUsername)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не существует"));

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не существует"));

        if (targetUser.getRole().equals(UserRole.BANNED)) {
            throw new IllegalArgumentException("Пользователь заблокирован");
        }
        if (requestingUser.equals(targetUser)) {
            throw new IllegalArgumentException("Нельзя подпсаться на себя");
        }

        Optional<Subscription> optionalSubscription = subscriptionRepository.findBySubscriberAndTarget(requestingUser, targetUser);
        if (optionalSubscription.isPresent()) {
            subscriptionRepository.delete(optionalSubscription.get());
            return new SubscriptionDto();
        } else {
            Subscription subscription = new Subscription(null, requestingUser, targetUser, null);
            subscription = subscriptionRepository.save(subscription);
           return modelMapper.map(subscription, SubscriptionDto.class);
        }
    }
}
