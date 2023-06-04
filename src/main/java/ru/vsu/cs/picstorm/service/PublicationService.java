package ru.vsu.cs.picstorm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import ru.vsu.cs.picstorm.dto.request.DateConstraint;
import ru.vsu.cs.picstorm.dto.request.PublicationReactionDto;
import ru.vsu.cs.picstorm.dto.request.SortConstraint;
import ru.vsu.cs.picstorm.dto.request.UserConstraint;
import ru.vsu.cs.picstorm.dto.response.PageDto;
import ru.vsu.cs.picstorm.dto.response.PictureDto;
import ru.vsu.cs.picstorm.dto.response.PublicationInfoDto;
import ru.vsu.cs.picstorm.entity.*;
import ru.vsu.cs.picstorm.repository.PictureRepository;
import ru.vsu.cs.picstorm.repository.PublicationRepository;
import ru.vsu.cs.picstorm.repository.ReactionRepository;
import ru.vsu.cs.picstorm.repository.UserRepository;
import ru.vsu.cs.picstorm.repository.specification.PublicationFeedSpecification;
import ru.vsu.cs.picstorm.util.PublicationRatingUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PublicationService {
    private final UserService userService;
    private final PictureStorageService pictureStorageService;
    private final PublicationRepository publicationRepository;
    private final ReactionRepository reactionRepository;
    private final PictureRepository pictureRepository;
    private final UserRepository userRepository;

    public void uploadPublication(String userNickname, byte[] uploadPicture) {
        User user = userRepository.findByNickname(userNickname)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не существует"));

        Picture picture = new Picture();
        picture = pictureRepository.save(picture);

        String publicationName = pictureStorageService.getPublicationName(picture);
        try {
            pictureStorageService.savePicture(publicationName, uploadPicture);
        } catch (Exception e) {
            pictureRepository.delete(picture);
            throw new RuntimeException("Ошибка при сохранении фото");
        }

        Publication publication = Publication.builder()
                .owner(user)
                .picture(picture)
                .state(PublicationState.VISIBLE)
                .reactions(new ArrayList<>())
                .build();
        publicationRepository.save(publication);
    }

    public PageDto<PublicationInfoDto> getPublicationFeed(String viewingUserNickname, DateConstraint dateConstraint, SortConstraint sortConstraint,
                                                          UserConstraint userConstraint, Long filterUserId, int index, int size) {
        Optional<User> viewer = userRepository.findByNickname(viewingUserNickname);
        if (viewer.isEmpty() && userConstraint.equals(UserConstraint.SUBSCRIPTIONS)) {
            throw new AccessDeniedException("Вы не можете просматривать ленту подпсиок");
        }
        User filterUser = null;
        if (filterUserId == null && userConstraint.equals(UserConstraint.SPECIFIED)) {
            throw new IllegalArgumentException("Укажите пользователя, чью ленту просматриваете");
        } else if (filterUserId != null) {
            filterUser = userRepository.findById(filterUserId)
                    .orElseThrow(() -> new NoSuchElementException("Пользователь не существует"));
        }
        PublicationFeedSpecification feedSpecification =
                new PublicationFeedSpecification(dateConstraint, userConstraint, viewer.orElse(null), filterUser);
        Pageable pageable = PageRequest.of(index, size, sortConstraint.toDataSort());
        Page<Publication> publicationPage = publicationRepository.findAll(feedSpecification, pageable);
        List<PublicationInfoDto> infoDtoList = publicationPage.stream()
                .map(publication -> buildPublicationInfoDto(publication, viewer))
                .toList();
        return new PageDto<>(infoDtoList, index, size, publicationPage.isLast());
    }

    private PublicationInfoDto buildPublicationInfoDto(Publication publication, Optional<User> viewer){
        PublicationInfoDto publicationInfo = new PublicationInfoDto();
        publicationInfo.setPublicationId(publication.getId());
        publicationInfo.setUploaded(publication.getCreated());
        publicationInfo.setRating(publication.getRating());

        User owner = publication.getOwner();
        publicationInfo.setOwnerId(owner.getId());
        publicationInfo.setOwnerNickname(owner.getNickname());

        if (viewer.isPresent()) {
            Optional<Reaction> reaction = reactionRepository.findByPublicationAndUser(publication, viewer.get());
            publicationInfo.setUserReaction(reaction.map(Reaction::getType).orElse(null));
        }
        return publicationInfo;
    }

    public PictureDto getPublicationPicture(long publicationId) {
        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new NoSuchElementException("Публикация не существует"));

        if (publication.getState() != PublicationState.VISIBLE) {
            throw new AccessDeniedException("Вы не можете просмаривать эту публикацию");
        }

        Picture publicationPicture = publication.getPicture();
        String pictureName = pictureStorageService.getPublicationName(publicationPicture);
        try {
            byte[] picture = pictureStorageService.getPicture(pictureName);
            return new PictureDto(picture);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при загрузке фото");
        }
    }

    public PublicationReactionDto setReaction(String userNickname, long publicationId, ReactionType newReaction) {
        User user = userRepository.findByNickname(userNickname)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не существует"));

        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new NoSuchElementException("Публикация не существует"));

        if (publication.getState() != PublicationState.VISIBLE) {
            throw new AccessDeniedException("Вы не можете оценивать эту публикацию");
        }

        Optional<Reaction> optionalReaction = reactionRepository.findByPublicationAndUser(publication, user);
        long ratingChange;
        Reaction reaction;
        if (optionalReaction.isPresent()) {
            reaction = optionalReaction.get();
            ratingChange = PublicationRatingUtils.calculateRatingChange(reaction.getType(), newReaction);
            reaction.setType(newReaction);
            reaction.setCreated(Instant.now());
        } else {
            reaction = new Reaction(null, newReaction, publication, user, null);
            ratingChange = PublicationRatingUtils.calculateRatingChange(null, newReaction);
        }
        reaction = reactionRepository.save(reaction);
        publication.setRating(publication.getRating() + ratingChange);
        publicationRepository.save(publication);
        return new PublicationReactionDto(reaction.getType());
    }

    public void banPublication(String userNickname, long publicationId) {
        userRepository.findByNickname(userNickname)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не существует"));

        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new NoSuchElementException("Публикация не существует"));

        publication.setState(PublicationState.BANNED);
        publicationRepository.save(publication);
    }

    public void deletePublication(String userNickname, long publicationId) {
        User user = userRepository.findByNickname(userNickname)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не существует"));

        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new NoSuchElementException("Публикация не существует"));

        if (!publication.getOwner().equals(user)) {
            throw new AccessDeniedException("Только владелец может удалять публикации");
        }

        Picture publicationPicture = publication.getPicture();
        String pictureName = pictureStorageService.getPublicationName(publicationPicture);
        try {
            pictureStorageService.deletePicture(pictureName);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при удалении фото");
        }
        publicationRepository.delete(publication);
        pictureRepository.delete(publicationPicture);
    }
}
