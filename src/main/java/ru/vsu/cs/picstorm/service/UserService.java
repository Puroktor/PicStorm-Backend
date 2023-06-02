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
import org.springframework.transaction.annotation.Transactional;
import ru.vsu.cs.picstorm.dto.response.PageDto;
import ru.vsu.cs.picstorm.dto.response.UserLineDto;
import ru.vsu.cs.picstorm.dto.response.UserProfileDto;
import ru.vsu.cs.picstorm.dto.response.UserRoleDto;
import ru.vsu.cs.picstorm.entity.*;
import ru.vsu.cs.picstorm.repository.PictureRepository;
import ru.vsu.cs.picstorm.repository.PublicationRepository;
import ru.vsu.cs.picstorm.repository.SubscriptionRepository;
import ru.vsu.cs.picstorm.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final PictureStorageService pictureStorageService;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PublicationRepository publicationRepository;
    private final PictureRepository pictureRepository;
    private final ModelMapper modelMapper;

    public PageDto<UserLineDto> findUsersByNickname(@Nullable String searchingUserNickname, @Nullable String searchNickname, int index, int size) {
        Pageable pageable = PageRequest.of(index, size, Sort.by("nickname"));
        searchNickname = searchNickname == null ? "" : searchNickname;
        User searchingUser = null;
        if (searchingUserNickname !=null) {
            searchingUser = userRepository.findByNickname(searchingUserNickname)
                    .orElseThrow(() -> new NoSuchElementException("Пользователь не существует"));
        }
        Page<User> usersPage = userRepository.findPageByNickname(searchNickname, pageable);
        List<UserLineDto> userDtoList = mapUserResultListForView(searchingUser, usersPage);
        return new PageDto<>(userDtoList, index, size, usersPage.isLast());
    }

    public List<UserLineDto> mapUserResultListForView(@Nullable User viewingUser, Streamable<User> usersPage) {
        return usersPage.stream().map(user -> {
            UserLineDto lineDto = new UserLineDto();
            lineDto.setUserId(user.getId());
            lineDto.setNickname(user.getNickname());
            byte[] avatar = getUserAvatar(user);
            lineDto.setAvatar(avatar);
            if (viewingUser != null && !viewingUser.equals(user)) {
                Optional<Subscription> subscription = subscriptionRepository.findBySubscriberAndTarget(viewingUser, user);
                lineDto.setSubscribed(subscription.isPresent());
            }
            return lineDto;
        }).toList();
    }

    public byte[] getUserAvatar(User user) {
        Picture avatar = user.getAvatar();
        if (avatar != null) {
            String avatarName = pictureStorageService.getAvatarName(user.getAvatar());
            try {
                return pictureStorageService.getPicture(avatarName);
            } catch (Exception e) {
                throw new RuntimeException("Ошибка при загрузке аватара");
            }
        }
        return null;
    }

    @Transactional
    public UserRoleDto banUser(String requesterUsername, Long userId) {
        userRepository.findByNickname(requesterUsername)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не существует"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не существует"));
        if (user.getRole() != UserRole.ORDINARY) {
            throw new AccessDeniedException("Нельзя заблокировать не обычного пользователя");
        }

        UserRoleDto userRoleDto = setUserRole(user, UserRole.BANNED);
        for (Publication publication : user.getPublications()) {
            publication.setState(PublicationState.USER_BANNED);
            publicationRepository.save(publication);
        }
        return userRoleDto;
    }

    public UserRoleDto changeAdminRole(String requesterUsername, Long userId) {
        userRepository.findByNickname(requesterUsername)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не существует"));
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
        if (user.getRole() == UserRole.BANNED) {
            throw new IllegalArgumentException("Пользователь заблокирован");
        }
        User requester = null;
        if (requesterUsername != null) {
            requester = userRepository.findByNickname(requesterUsername)
                    .orElseThrow(() -> new NoSuchElementException("Пользователь не существует"));
        }
        UserProfileDto profileDto = modelMapper.map(user, UserProfileDto.class);
        byte[] avatar = getUserAvatar(user);
        profileDto.setAvatar(avatar);
        if (requester != null && !requester.equals(user)) {
            Optional<Subscription> subscription = subscriptionRepository.findBySubscriberAndTarget(requester, user);
            profileDto.setSubscribed(subscription.isPresent());
        }
        long subscriptionsCount = subscriptionRepository.countBySubscriber(user);
        profileDto.setSubscriptionsCount(subscriptionsCount);
        long subscribersCount = subscriptionRepository.countByTarget(user);
        profileDto.setSubscribersCount(subscribersCount);
        return profileDto;
    }

    public void uploadAvatar(String userNickname, byte[] uploadPicture) {
        User user = userRepository.findByNickname(userNickname)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не существует"));

        Picture newAvatar = new Picture();
        newAvatar = pictureRepository.save(newAvatar);

        String avatarName = pictureStorageService.getAvatarName(newAvatar);
        try {
            pictureStorageService.savePicture(avatarName, uploadPicture);
        } catch (Exception e) {
            pictureRepository.delete(newAvatar);
            throw new RuntimeException("Ошибка при сохранении аватара");
        }

        Picture oldAvatar = user.getAvatar();
        if (oldAvatar!= null){
            removeAvatar(oldAvatar);
        }
        user.setAvatar(newAvatar);
        userRepository.save(user);
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
