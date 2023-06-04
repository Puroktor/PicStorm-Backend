package ru.vsu.cs.picstorm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.picstorm.dto.request.DateConstraint;
import ru.vsu.cs.picstorm.dto.request.PublicationReactionDto;
import ru.vsu.cs.picstorm.dto.request.SortConstraint;
import ru.vsu.cs.picstorm.dto.request.UserConstraint;
import ru.vsu.cs.picstorm.dto.response.PageDto;
import ru.vsu.cs.picstorm.dto.response.PictureDto;
import ru.vsu.cs.picstorm.dto.response.PublicationInfoDto;
import ru.vsu.cs.picstorm.entity.ReactionType;
import ru.vsu.cs.picstorm.service.PublicationService;

import java.util.Optional;

@RestController
@RequestMapping("/api/publication/")
@RequiredArgsConstructor
@Validated
@Tag(name = "Publication API", description = "Allows to upload, view, ban and delete publications")
@SecurityRequirement(name = "JWT Authentication")
public class PublicationController {
    public final static int MAX_PUBLICATION_PICTURE_SIZE = 1024 * 1024;
    private final PublicationService publicationService;

    @Operation(summary = "Uploads new publication")
    @PostMapping
    @PreAuthorize("hasAuthority('UPLOAD_AUTHORITY')")
    public ResponseEntity<Void> uploadPublication(
            @RequestBody @NotNull(message = "Предоставьте фото для загурзки")
            @Size(min = 1, max = MAX_PUBLICATION_PICTURE_SIZE, message = "Неверный размер фото для загрузки")
            @Parameter(description = "Picture to upload )", example = "QkFTRTY0U1RSSU5HZHNhZGFzZGFzYXNkZHNh") byte[] uploadPicture,
            @Parameter(hidden = true) Authentication authentication) {
        String userNickname = authentication.getName();
        publicationService.uploadPublication(userNickname, uploadPicture);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @Operation(summary = "Returns publication feed with appalled filters")
    @GetMapping("feed")
    public ResponseEntity<PageDto<PublicationInfoDto>> getPublicationFeed(
            @RequestParam(value = "dateFilter") @Parameter(description = "Date filter constraint", example = "NONE")
            @NotNull(message = "Предоставьте фильтр по дате") DateConstraint dateConstraint,
            @RequestParam(value = "sortFilter") @Parameter(description = "Sort filter constraint", example = "LIKED_FIRST")
            @NotNull(message = "Предоставьте фильтр по рейтингу") SortConstraint sortConstraint,
            @RequestParam(value = "userFilter") @Parameter(description = "User filter constraint", example = "SPECIFIED")
            @NotNull(message = "Предоставьте фильтр по пользователям") UserConstraint userConstraint,
            @RequestParam(value = "filterUser", required = false) @Parameter(description = "Specified filter user id", example = "1") Long filterUserId,
            @RequestParam("index") @Min(value = 0, message = "Индекс страницы должен быть >=0")
            @Parameter(description = "Index of desired page", example = "0") int index,
            @RequestParam("size") @Min(value = 1, message = "Размер страницы должен быть >=1")
            @Parameter(description = "Size of pages", example = "5") int size,
            @Parameter(hidden = true) Authentication authentication) {
        String userNickname = Optional.ofNullable(authentication).map(Authentication::getName).orElse(null);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(publicationService.getPublicationFeed(userNickname, dateConstraint, sortConstraint, userConstraint, filterUserId, index, size));
    }

    @Operation(summary = "Returns publication picture by id")
    @GetMapping("{publicationId}/picture")
    public ResponseEntity<PictureDto> getPublicationPicture(@PathVariable @Parameter(description = "Publication Id", example = "4") long publicationId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(publicationService.getPublicationPicture(publicationId));
    }


    @Operation(summary = "Sets user publication reaction")
    @PutMapping("{publicationId}/reaction")
    @PreAuthorize("hasAuthority('REACT_AUTHORITY')")
    public ResponseEntity<PublicationReactionDto> setReaction(@PathVariable @Parameter(description = "Publication Id", example = "7") long publicationId,
                                                              @RequestParam(value = "reaction", required = false)
                                                              @Parameter(description = "New reaction", example = "DISLIKE") ReactionType reactionType,
                                                              @Parameter(hidden = true) Authentication authentication) {
        String userNickname = authentication.getName();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(publicationService.setReaction(userNickname, publicationId, reactionType));
    }

    @Operation(summary = "Bans publication")
    @PutMapping("{publicationId}")
    @PreAuthorize("hasAuthority('BAN_PUBLICATION_AUTHORITY')")
    public ResponseEntity<Void> banPublication(@PathVariable @Parameter(description = "Publication Id", example = "5") long publicationId,
                                               @Parameter(hidden = true) Authentication authentication) {
        String userNickname = authentication.getName();
        publicationService.banPublication(userNickname, publicationId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @Operation(summary = "Deletes publication (only for owner)")
    @DeleteMapping("{publicationId}")
    @PreAuthorize("hasAuthority('UPLOAD_AUTHORITY')")
    public ResponseEntity<Void> deletePublication(@PathVariable @Parameter(description = "Publication Id", example = "10") long publicationId,
                                                  @Parameter(hidden = true) Authentication authentication) {
        String userNickname = authentication.getName();
        publicationService.deletePublication(userNickname, publicationId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
}
