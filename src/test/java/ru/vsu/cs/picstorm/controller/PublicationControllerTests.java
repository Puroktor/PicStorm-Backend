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
import ru.vsu.cs.picstorm.dto.request.DateConstraint;
import ru.vsu.cs.picstorm.dto.request.PublicationReactionDto;
import ru.vsu.cs.picstorm.dto.request.SortConstraint;
import ru.vsu.cs.picstorm.dto.request.UserConstraint;
import ru.vsu.cs.picstorm.dto.response.PageDto;
import ru.vsu.cs.picstorm.dto.response.PictureDto;
import ru.vsu.cs.picstorm.dto.response.PublicationInfoDto;
import ru.vsu.cs.picstorm.entity.ReactionType;
import ru.vsu.cs.picstorm.service.PublicationService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class PublicationControllerTests {
    @MockBean
    private Authentication authentication;
    @MockBean
    private PublicationService publicationService;
    @Autowired
    private PublicationController publicationController;

    private static final String MOCK_USERNAME = "username";

    @BeforeEach
    public void prepareTest() {
        when(authentication.getName()).thenReturn(MOCK_USERNAME);
    }

    @Test
    public void getPublicationFeedWithValidParams() {
        DateConstraint dateConstraint = DateConstraint.WEEK;
        SortConstraint sortConstraint = SortConstraint.NONE;
        UserConstraint userConstraint = UserConstraint.SPECIFIED;
        long userId = 1;
        int index = 0, size = 1;
        PageDto<PublicationInfoDto> pageDto = new PageDto<>();

        when(publicationService.getPublicationFeed(MOCK_USERNAME, dateConstraint, sortConstraint,
                userConstraint, userId, index, size)).thenReturn(pageDto);
        ResponseEntity<?> returned = publicationController.getPublicationFeed(dateConstraint, sortConstraint,
                userConstraint, userId, index, size, authentication);

        assertEquals(HttpStatus.OK, returned.getStatusCode());
        assertEquals(pageDto, returned.getBody());
        verify(publicationService, times(1)).getPublicationFeed(MOCK_USERNAME, dateConstraint, sortConstraint,
                userConstraint, userId, index, size);
    }

    @Test
    public void getSubscriptionsWithInvalidValidIndex() {
        DateConstraint dateConstraint = DateConstraint.WEEK;
        SortConstraint sortConstraint = SortConstraint.NONE;
        UserConstraint userConstraint = UserConstraint.SPECIFIED;
        long userId = 1;
        int index = -1, size = 1;
        assertThrows(ValidationException.class, () -> publicationController.getPublicationFeed(dateConstraint, sortConstraint,
                userConstraint, userId, index, size, authentication));
        verify(publicationService, times(0)).getPublicationFeed(anyString(), any(), any(), any(), anyLong(), anyInt(), anyInt());
    }

    @Test
    public void getSubscriptionsWithInvalidValidSize() {
        DateConstraint dateConstraint = DateConstraint.WEEK;
        SortConstraint sortConstraint = SortConstraint.NONE;
        UserConstraint userConstraint = UserConstraint.SPECIFIED;
        long userId = 1;
        int index = 1, size = 0;
        assertThrows(ValidationException.class, () -> publicationController.getPublicationFeed(dateConstraint, sortConstraint,
                userConstraint, userId, index, size, authentication));
        verify(publicationService, times(0)).getPublicationFeed(anyString(), any(), any(), any(), anyLong(), anyInt(), anyInt());
    }

    @Test
    public void getSubscriptionsWithoutConstraints() {
        long userId = 1;
        int index = 1, size = 1;
        assertThrows(ValidationException.class, () -> publicationController.getPublicationFeed(null, null, null,
                userId, index, size, authentication));
        verify(publicationService, times(0)).getPublicationFeed(anyString(), any(), any(), any(), anyLong(), anyInt(), anyInt());
    }

    @Test
    public void uploadPublicationAsAuthorized() {
        byte[] picture = new byte[] {0};
        assertThrows(AuthenticationException.class, () -> publicationController.uploadPublication(picture, authentication));
        verify(publicationService, times(0)).uploadPublication(anyString(), any());
    }

    @Test
    @WithMockUser(authorities = "UPLOAD_AUTHORITY")
    public void uploadEmptyPublication() {
        assertThrows(ValidationException.class, () -> publicationController.uploadPublication(null, authentication));
        verify(publicationService, times(0)).uploadPublication(anyString(), any());
    }

    @Test
    @WithMockUser(authorities = "UPLOAD_AUTHORITY")
    public void uploadTooBigPublication() {
        byte[] picture = new byte[PublicationController.MAX_PUBLICATION_PICTURE_SIZE + 1];
        assertThrows(ValidationException.class, () -> publicationController.uploadPublication(picture, authentication));
        verify(publicationService, times(0)).uploadPublication(anyString(), any());
    }

    @Test
    @WithMockUser(authorities = "UPLOAD_AUTHORITY")
    public void uploadValidPublication() {
        byte[] uploadPicture = new byte[] {0};
        ResponseEntity<?> returned = publicationController.uploadPublication(uploadPicture, authentication);

        assertEquals(HttpStatus.CREATED, returned.getStatusCode());
        verify(publicationService, times(1)).uploadPublication(MOCK_USERNAME, uploadPicture);
    }

    @Test
    public void getPublicationPictureWithValidParams() {
        long publicationId = 0;
        byte[] picture = new byte[0];
        PictureDto pictureDto = new PictureDto(picture);
        when(publicationService.getPublicationPicture(publicationId)).thenReturn(pictureDto);
        ResponseEntity<PictureDto> returned = publicationController.getPublicationPicture(publicationId);

        assertEquals(HttpStatus.OK, returned.getStatusCode());
        assertEquals(pictureDto, returned.getBody());
        verify(publicationService, times(1)).getPublicationPicture(publicationId);
    }

    @Test
    public void setReactionAsUnauthorized() {
        assertThrows(AuthenticationException.class, () -> publicationController.setReaction(1, new PublicationReactionDto(), authentication));
        verify(publicationService, times(0)).setReaction(anyString(), anyLong(), any());
    }

    @Test
    @WithMockUser(authorities = "REACT_AUTHORITY")
    public void setEmptyReaction() {
        assertThrows(ValidationException.class, () -> publicationController.setReaction(1, new PublicationReactionDto(), authentication));
        verify(publicationService, times(0)).setReaction(anyString(), anyLong(), any());
    }

    @Test
    @WithMockUser(authorities = "REACT_AUTHORITY")
    public void setValidReaction() {
        long publicationId = 0;
        PublicationReactionDto reactionDto = new PublicationReactionDto(ReactionType.LIKE);
        when(publicationService.setReaction(MOCK_USERNAME, publicationId, reactionDto)).thenReturn(reactionDto);
        ResponseEntity<?> returned = publicationController.setReaction(publicationId, reactionDto, authentication);

        assertEquals(HttpStatus.OK, returned.getStatusCode());
        assertEquals(reactionDto, returned.getBody());
        verify(publicationService, times(1)).setReaction(MOCK_USERNAME, publicationId, reactionDto);
    }

    @Test
    @WithMockUser(authorities = "BAN_PUBLICATION_AUTHORITY")
    public void banPublicationWithNecessaryAuthority() {
        long publicationId = 0;
        ResponseEntity<?> returned = publicationController.banPublication(publicationId, authentication);

        assertEquals(HttpStatus.OK, returned.getStatusCode());
        verify(publicationService, times(1)).banPublication(MOCK_USERNAME, publicationId);
    }

    @Test
    public void banPublicationWithoutAuthority() {
        assertThrows(AuthenticationException.class, () -> publicationController.banPublication(0, authentication));
        verify(publicationService, times(0)).banPublication(anyString(), anyLong());
    }

    @Test
    public void deletePublicationAsUnauthorized() {
        assertThrows(AuthenticationException.class, () -> publicationController.deletePublication(1, authentication));
        verify(publicationService, times(0)).deletePublication(anyString(), anyLong());
    }

    @Test
    @WithMockUser(authorities = "UPLOAD_AUTHORITY")
    public void deleteValidPublication() {
        long publicationId = 0;
        ResponseEntity<?> returned = publicationController.deletePublication(publicationId, authentication);

        assertEquals(HttpStatus.OK, returned.getStatusCode());
        verify(publicationService, times(1)).deletePublication(MOCK_USERNAME, publicationId);
    }
}
