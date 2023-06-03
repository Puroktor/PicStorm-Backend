package ru.vsu.cs.picstorm.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import ru.vsu.cs.picstorm.dto.request.PublicationReactionDto;
import ru.vsu.cs.picstorm.dto.response.PictureDto;
import ru.vsu.cs.picstorm.entity.*;
import ru.vsu.cs.picstorm.repository.PictureRepository;
import ru.vsu.cs.picstorm.repository.PublicationRepository;
import ru.vsu.cs.picstorm.repository.ReactionRepository;
import ru.vsu.cs.picstorm.repository.UserRepository;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class PublicationServiceTests {
    @MockBean
    private PictureStorageService pictureStorageService;
    @MockBean
    private PublicationRepository publicationRepository;
    @MockBean
    private ReactionRepository reactionRepository;
    @MockBean
    private PictureRepository pictureRepository;
    @MockBean
    private UserRepository userRepository;
    @Autowired
    private PublicationService publicationService;

    @Test
    public void uploadPublication() throws Exception {
        String nickname = "name";
        String publicationName = "publication";
        byte[] photo = new byte[]{0};
        User user = User.builder().id(1L).build();
        Picture picture = Picture.builder().id(1L).build();

        when(userRepository.findByNickname(nickname)).thenReturn(Optional.of(user));
        when(pictureRepository.save(any())).thenReturn(picture);
        when(pictureStorageService.getPublicationName(picture)).thenReturn(publicationName);

        publicationService.uploadPublication(nickname, photo);

        verify(pictureRepository, times(1)).save(any());
        verify(pictureStorageService, times(1)).getPublicationName(picture);
        verify(pictureStorageService, times(1)).savePicture(publicationName, photo);
        verify(publicationRepository, times(1)).save(argThat(publication -> {
            assertEquals(user, publication.getOwner());
            assertEquals(picture, publication.getPicture());
            assertEquals(PublicationState.VISIBLE, publication.getState());
            return true;
        }));
    }

    @Test
    public void uploadPublicationWithExceptionDuringLoading() throws Exception {
        String nickname = "name";
        String publicationName = "publication";
        byte[] photo = new byte[]{0};
        User user = User.builder().id(1L).build();
        Picture picture = Picture.builder().id(1L).build();

        when(userRepository.findByNickname(nickname)).thenReturn(Optional.of(user));
        when(pictureRepository.save(any())).thenReturn(picture);
        when(pictureStorageService.getPublicationName(picture)).thenReturn(publicationName);
        doThrow(Exception.class).when(pictureStorageService).savePicture(publicationName, photo);

        assertThrows(RuntimeException.class, () -> publicationService.uploadPublication(nickname, photo));

        verify(pictureRepository, times(1)).save(any());
        verify(pictureRepository, times(1)).delete(picture);
        verify(pictureStorageService, times(1)).getPublicationName(picture);
        verify(pictureStorageService, times(1)).savePicture(publicationName, photo);
        verify(publicationRepository, times(0)).save(any());
    }

    @Test
    public void getPublicationPicture() throws Exception {
        long publicationId = 1L;
        String pictureName = "picture";
        byte[] pictureData = new byte[10];
        Picture picture = new Picture();
        Publication publication = Publication.builder().id(publicationId).picture(picture).state(PublicationState.VISIBLE).build();

        when(publicationRepository.findById(publicationId)).thenReturn(Optional.of(publication));
        when(pictureStorageService.getPublicationName(any())).thenReturn(pictureName);
        when(pictureStorageService.getPicture(pictureName)).thenReturn(pictureData);

        PictureDto returnedPicture = publicationService.getPublicationPicture(publicationId);

        assertArrayEquals(returnedPicture.getPicture(), pictureData);
        verify(pictureStorageService, times(1)).getPicture(pictureName);
    }

    @Test
    public void getBannedPublicationPicture() {
        long publicationId = 1L;
        Publication publication = Publication.builder().id(publicationId).state(PublicationState.USER_BANNED).build();
        when(publicationRepository.findById(publicationId)).thenReturn(Optional.of(publication));
        assertThrows(AccessDeniedException.class, () -> publicationService.getPublicationPicture(publicationId));
    }

    @Test
    public void setReactionToBannedPublication() {
        long publicationId = 1L;
        Publication publication = Publication.builder().id(publicationId).state(PublicationState.BANNED).build();
        when(userRepository.findByNickname(any())).thenReturn(Optional.of(new User()));
        when(publicationRepository.findById(publicationId)).thenReturn(Optional.of(publication));
        assertThrows(AccessDeniedException.class, () -> publicationService.setReaction("name", publicationId, new PublicationReactionDto()));
    }

    @Test
    public void setNewLikeReaction() {
        long publicationId = 1L;
        String nickname = "nickname";
        User user = User.builder().id(1L).build();
        Publication publication = Publication.builder().id(publicationId).state(PublicationState.VISIBLE).rating(0L).build();
        PublicationReactionDto reactionDto = new PublicationReactionDto(ReactionType.LIKE);

        when(userRepository.findByNickname(nickname)).thenReturn(Optional.of(user));
        when(publicationRepository.findById(publicationId)).thenReturn(Optional.of(publication));
        when(reactionRepository.findByPublicationAndUser(publication, user)).thenReturn(Optional.empty());
        when(reactionRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        publicationService.setReaction(nickname, publicationId, reactionDto);

        verify(reactionRepository, times(1)).save(any());
        verify(publicationRepository, times(1)).save(argThat(publ -> {
            assertEquals(1L, publ.getRating());
            return true;
        }));
    }

    @Test
    public void removeLikeReaction() {
        long publicationId = 1L;
        String nickname = "nickname";
        User user = User.builder().id(1L).build();
        Publication publication = Publication.builder().id(publicationId).state(PublicationState.VISIBLE).rating(1L).build();
        Reaction oldReaction = Reaction.builder().type(ReactionType.LIKE).build();
        PublicationReactionDto reactionDto = new PublicationReactionDto(null);

        when(userRepository.findByNickname(nickname)).thenReturn(Optional.of(user));
        when(publicationRepository.findById(publicationId)).thenReturn(Optional.of(publication));
        when(reactionRepository.findByPublicationAndUser(publication, user)).thenReturn(Optional.of(oldReaction));
        when(reactionRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        publicationService.setReaction(nickname, publicationId, reactionDto);

        verify(reactionRepository, times(1)).save(any());
        verify(publicationRepository, times(1)).save(argThat(publ -> {
            assertEquals(0L, publ.getRating());
            return true;
        }));

    }

    @Test
    public void setDislikeInsteadOfLikeReaction() {
        long publicationId = 1L;
        String nickname = "nickname";
        User user = User.builder().id(1L).build();
        Publication publication = Publication.builder().id(publicationId).state(PublicationState.VISIBLE).rating(1L).build();
        PublicationReactionDto reactionDto = new PublicationReactionDto(ReactionType.DISLIKE);
        Reaction reaction = new Reaction(1L, ReactionType.LIKE, publication, user, Instant.now());

        when(userRepository.findByNickname(nickname)).thenReturn(Optional.of(user));
        when(publicationRepository.findById(publicationId)).thenReturn(Optional.of(publication));
        when(reactionRepository.findByPublicationAndUser(publication, user)).thenReturn(Optional.of(reaction));
        when(reactionRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        publicationService.setReaction(nickname, publicationId, reactionDto);

        verify(reactionRepository, times(1)).save(any());
        verify(publicationRepository, times(1)).save(argThat(publ -> {
            assertEquals(-1L, publ.getRating());
            return true;
        }));
    }

    @Test
    public void banPublication() {
        String nickname = "name";
        long publicationId = 1L;
        User user = User.builder().id(1L).build();
        Publication publication = Publication.builder().id(publicationId).owner(user).build();

        when(userRepository.findByNickname(nickname)).thenReturn(Optional.of(user));
        when(publicationRepository.findById(publicationId)).thenReturn(Optional.of(publication));

        publicationService.banPublication(nickname, publicationId);

        verify(publicationRepository, times(1)).save(argThat(publ -> {
            assertEquals(PublicationState.BANNED, publ.getState());
            return true;
        }));
    }

    @Test
    public void deletePublication() throws Exception {
        String nickname = "name";
        String publicationName = "publication";
        long publicationId = 1L;
        User user = User.builder().id(1L).build();
        Picture picture = Picture.builder().id(1L).build();
        Publication publication = Publication.builder().id(publicationId).owner(user).picture(picture).build();

        when(userRepository.findByNickname(nickname)).thenReturn(Optional.of(user));
        when(publicationRepository.findById(publicationId)).thenReturn(Optional.of(publication));
        when(pictureStorageService.getPublicationName(any())).thenReturn(publicationName);

        publicationService.deletePublication(nickname, publicationId);

        verify(pictureStorageService, times(1)).deletePicture(publicationName);
        verify(pictureRepository, times(1)).delete(picture);
        verify(publicationRepository, times(1)).delete(publication);
    }

    @Test
    public void deleteNotOwningPublication() throws Exception {
        String nickname = "name";
        long publicationId = 1L;
        User user1 = User.builder().id(1L).build();
        User user2 = User.builder().id(2L).build();
        Publication publication = Publication.builder().id(publicationId).owner(user2).build();

        when(userRepository.findByNickname(nickname)).thenReturn(Optional.of(user1));
        when(publicationRepository.findById(publicationId)).thenReturn(Optional.of(publication));

        assertThrows(AccessDeniedException.class, () -> publicationService.deletePublication(nickname, publicationId));

        verify(pictureStorageService, times(0)).deletePicture(any());
        verify(pictureRepository, times(0)).delete(any());
        verify(publicationRepository, times(0)).delete(any(Publication.class));
    }

    @Test
    public void deletePublicationWithExceptionDuringRemoval() throws Exception {
        String nickname = "name";
        String publicationName = "publication";
        long publicationId = 1L;
        User user = User.builder().id(1L).build();
        Publication publication = Publication.builder().id(publicationId).owner(user).build();

        when(userRepository.findByNickname(nickname)).thenReturn(Optional.of(user));
        when(publicationRepository.findById(publicationId)).thenReturn(Optional.of(publication));
        when(pictureStorageService.getPublicationName(any())).thenReturn(publicationName);
        doThrow(Exception.class).when(pictureStorageService).deletePicture(publicationName);

        assertThrows(RuntimeException.class, () -> publicationService.deletePublication(nickname, publicationId));

        verify(pictureStorageService, times(1)).deletePicture(publicationName);
        verify(pictureRepository, times(0)).delete(any());
        verify(publicationRepository, times(0)).delete(any(Publication.class));
    }
}
