package ru.vsu.cs.picstorm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import ru.vsu.cs.picstorm.dto.request.PublicationReactionDto;
import ru.vsu.cs.picstorm.dto.request.UploadPictureDto;
import ru.vsu.cs.picstorm.dto.response.ResponsePictureDto;
import ru.vsu.cs.picstorm.entity.*;
import ru.vsu.cs.picstorm.repository.PictureRepository;
import ru.vsu.cs.picstorm.repository.PublicationRepository;
import ru.vsu.cs.picstorm.repository.ReactionRepository;
import ru.vsu.cs.picstorm.repository.UserRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;

import static ru.vsu.cs.picstorm.entity.RoleAuthority.BAN_USER_AUTHORITY;

@Service
@RequiredArgsConstructor
public class PublicationService {
    private final UserService userService;
    private final PictureStorageService pictureStorageService;
    private final PublicationRepository publicationRepository;
    private final ReactionRepository reactionRepository;
    private final PictureRepository pictureRepository;
    private final UserRepository userRepository;

    public void uploadPublication(String userNickname, UploadPictureDto uploadPictureDto) {
        User user = userRepository.findByNickname(userNickname)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не существует"));

        Picture picture = new Picture();
        picture.setPictureType(uploadPictureDto.getPictureType());
        picture = pictureRepository.save(picture);

        String publicationName = pictureStorageService.getPublicationName(picture);
        try {
            pictureStorageService.savePicture(publicationName, uploadPictureDto.getPicture());
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

    public ResponsePictureDto getPublicationPicture(long publicationId) {
        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new NoSuchElementException("Публикация не существует"));

        if (publication.getState() != PublicationState.VISIBLE) {
            throw new AccessDeniedException("Вы не можете просмаривать эту публикацию");
        }

        Picture publicationPicture = publication.getPicture();
        String pictureName = pictureStorageService.getPublicationName(publicationPicture);
        byte[] pictureData;
        try {
            pictureData = pictureStorageService.getPicture(pictureName);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при загрузке фото");
        }

        return new ResponsePictureDto(publicationPicture.getPictureType(), pictureData);
    }

    public PublicationReactionDto setReaction(String userNickname, long publicationId, PublicationReactionDto reactionDto) {
        User user = userRepository.findByNickname(userNickname)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не существует"));

        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new NoSuchElementException("Публикация не существует"));

        if (publication.getState() != PublicationState.VISIBLE) {
            throw new AccessDeniedException("Вы не можете оценивать эту публикацию");
        }

        ReactionType newReaction = reactionDto.getReaction();
        Optional<Reaction> optionalReaction = reactionRepository.findByPublicationAndUser(publication, user);
        long ratingChange;
        Reaction reaction;
        if (optionalReaction.isPresent()) {
            reaction = optionalReaction.get();
            ratingChange = newReaction.calculateRatingChange(reaction.getType());
            reaction.setType(newReaction);
            reaction.setCreated(Instant.now());
        } else {
            reaction = new Reaction(null, newReaction, publication, user, null);
            ratingChange = newReaction.calculateRatingChange(null);
        }
        reaction = reactionRepository.save(reaction);
        publication.setRating(publication.getRating() - ratingChange);
        publicationRepository.save(publication);
        return new PublicationReactionDto(reaction.getType());
    }

    public void banPublication(String userNickname, long publicationId) {
        User user = userRepository.findByNickname(userNickname)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не существует"));

        if (!user.getRole().getAuthorities().contains(BAN_USER_AUTHORITY)) {
            throw new AccessDeniedException("У вас нет права блокировать публикации");
        }

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
        pictureRepository.delete(publicationPicture);
        publicationRepository.delete(publication);
    }
}
