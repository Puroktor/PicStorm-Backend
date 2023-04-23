package ru.vsu.cs.picstorm.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Streamable;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import ru.vsu.cs.picstorm.dto.response.*;
import ru.vsu.cs.picstorm.entity.*;
import ru.vsu.cs.picstorm.repository.PictureRepository;
import ru.vsu.cs.picstorm.repository.PublicationRepository;
import ru.vsu.cs.picstorm.repository.SubscriptionRepository;
import ru.vsu.cs.picstorm.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static ru.vsu.cs.picstorm.entity.RoleAuthority.BAN_USER_AUTHORITY;
import static ru.vsu.cs.picstorm.entity.RoleAuthority.MANAGE_ADMINS_AUTHORITY;

@Service
@RequiredArgsConstructor
public class UserService {
    private final PictureStorageService pictureStorageService;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PublicationRepository publicationRepository;
    private final PictureRepository pictureRepository;
    private final ModelMapper modelMapper;

    public PageDto<UserLineDto> findUsersByNickname(@Nullable String searchingUseNickname, @Nullable String searchNickname, int index, int size) {
        Pageable pageable = PageRequest.of(index, size, Sort.by("nickname"));
        searchNickname = searchNickname == null ? "" : searchNickname;
        Page<User> usersPage = userRepository.findPageByNickname(searchNickname, pageable);
        List<UserLineDto> userDtoList = mapUserResultListForView(searchingUseNickname, usersPage);
        return new PageDto<>(userDtoList, index, size, usersPage.isLast());
    }

    public List<UserLineDto> mapUserResultListForView(@Nullable String viewingUseNickname, Streamable<User> usersPage) {
        return usersPage.stream().map(user -> {
            UserLineDto lineDto = new UserLineDto();
            lineDto.setUserId(user.getId());
            lineDto.setNickname(user.getNickname());
            ResponsePictureDto avatarDto = getUserAvatar(user);
            lineDto.setAvatar(avatarDto);
            Optional<Subscription> subscription = getSubscription(viewingUseNickname, user);
            lineDto.setSubscribed(subscription.isPresent());
            return lineDto;
        }).toList();
    }

    public ResponsePictureDto getUserAvatar(User user) {
        Picture avatar = user.getAvatar();
        if (avatar != null) {
            String avatarName = pictureStorageService.getAvatarName(user.getAvatar());
            byte[] avatarData;
            try {
                avatarData = pictureStorageService.getPicture(avatarName);
            } catch (Exception e) {
                throw new RuntimeException("Ошибка при загрузке аватара");
            }
            return new ResponsePictureDto(avatar.getPictureType(), avatarData);
        }
        return null;
    }

    private Optional<Subscription> getSubscription(@Nullable String subscriberName, User tagetUser) {
        if (subscriberName != null) {
            User viewingUser = userRepository.findByNickname(subscriberName)
                    .orElseThrow(() -> new NoSuchElementException("Пользователь не существует"));
            return subscriptionRepository.findBySubscriberAndTarget(viewingUser, tagetUser);
        }
        return Optional.empty();
    }

    public UserRoleDto banUser(String requesterUsername, Long userId) {
        User requestingUser = userRepository.findByNickname(requesterUsername)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не существует"));
        if (!requestingUser.getRole().getAuthorities().contains(BAN_USER_AUTHORITY)) {
            throw new AccessDeniedException("У вас нет права блокировать пользователей");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не существует"));
        if (user.getRole() != UserRole.ORDINARY) {
            throw new AccessDeniedException("Нельзя заблокировать не обычного пользователя");
        }

        UserRoleDto userRoleDto = setUserRole(user, UserRole.BANNED);
        List<Publication> publications = publicationRepository.findAllByOwner(user);
        for (Publication publication : publications) {
            publication.setState(PublicationState.USER_BANNED);
            publicationRepository.save(publication);
        }
        return userRoleDto;
    }

    public UserRoleDto changeAdminRole(String requesterUsername, Long userId) {
        User requestingUser = userRepository.findByNickname(requesterUsername)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не существует"));
        if (!requestingUser.getRole().getAuthorities().contains(MANAGE_ADMINS_AUTHORITY)) {
            throw new AccessDeniedException("У вас нет права управлять ролями администраторов");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не существует"));

        return switch (user.getRole()) {
            case ADMIN -> setUserRole(user, UserRole.ORDINARY);
            case ORDINARY -> setUserRole(user, UserRole.ADMIN);
            default -> throw new AccessDeniedException("У данного пользователя нельзя сменить роль");
        };
    }

    private UserRoleDto setUserRole(User user, UserRole role) {
        user.setRole(role);
        userRepository.save(user);
        return new UserRoleDto(user.getId(), role);
    }

    public UserProfileDto getUserProfile(@Nullable String requesterUsername, long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не существует"));
        UserProfileDto profileDto = modelMapper.map(user, UserProfileDto.class);
        ResponsePictureDto avatarDto = getUserAvatar(user);
        profileDto.setAvatar(avatarDto);
        Optional<Subscription> subscription = getSubscription(requesterUsername, user);
        profileDto.setSubscribed(subscription.isPresent());
        long subscriptionsCount = subscriptionRepository.countBySubscriber(user);
        profileDto.setSubscriptionsCount(subscriptionsCount);
        long subscribersCount = subscriptionRepository.countByTarget(user);
        profileDto.setSubscribersCount(subscribersCount);
        return profileDto;
    }

    private void removeAvatar(Picture oldAvatar) {
        String avatarName = pictureStorageService.getAvatarName(oldAvatar);
        try {
            pictureStorageService.deletePicture(avatarName);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при удалении аватара");
        }
        pictureRepository.delete(oldAvatar);
    }
}
