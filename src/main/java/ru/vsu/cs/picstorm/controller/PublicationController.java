package ru.vsu.cs.picstorm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import ru.vsu.cs.picstorm.dto.response.PublicationInfoDto;
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
            @Size(min = 1, max = MAX_PUBLICATION_PICTURE_SIZE, message = "Неверный размер фото для загрузки") byte[] uploadPicture,
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
            @RequestParam(value = "dateFilter") @Parameter(description = "Date filter constraint")
            @NotNull(message = "Предоставьте фильтр по дате") DateConstraint dateConstraint,
            @RequestParam(value = "sortFilter") @Parameter(description = "Sort filter constraint")
            @NotNull(message = "Предоставьте фильтр по рейтингу") SortConstraint sortConstraint,
            @RequestParam(value = "userFilter") @Parameter(description = "User filter constraint")
            @NotNull(message = "Предоставьте фильтр по пользователям") UserConstraint userConstraint,
            @RequestParam(value = "filterUser", required = false) @Parameter(description = "Specified filter user id") Long filterUserId,
            @RequestParam("index") @Min(value = 0, message = "Индекс страницы должен быть >=0")
            @Parameter(description = "Index of desired page", example = "1") int index,
            @RequestParam("size") @Min(value = 1, message = "Размер страницы должен быть >=1")
            @Parameter(description = "Size of pages", example = "1") int size,
            @Parameter(hidden = true) Authentication authentication) {
        String userNickname = Optional.ofNullable(authentication).map(Authentication::getName).orElse(null);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(publicationService.getPublicationFeed(userNickname, dateConstraint, sortConstraint, userConstraint, filterUserId, index, size));
    }

    @Operation(summary = "Returns publication picture by id")
    @GetMapping("{publicationId}/picture")
    public ResponseEntity<byte[]> getPublicationPicture(@PathVariable long publicationId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(publicationService.getPublicationPicture(publicationId));
    }


    @Operation(summary = "Sets user publication reaction")
    @PutMapping("{publicationId}/reaction")
    @PreAuthorize("hasAuthority('REACT_AUTHORITY')")
    public ResponseEntity<PublicationReactionDto> setReaction(@PathVariable long publicationId,
            @RequestBody @NotNull(message = "Предоставьте свою реакцию") @Valid PublicationReactionDto reactionDto,
            @Parameter(hidden = true) Authentication authentication) {
        String userNickname = authentication.getName();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(publicationService.setReaction(userNickname, publicationId, reactionDto));
    }

    @Operation(summary = "Bans publication")
    @PutMapping("{publicationId}")
    @PreAuthorize("hasAuthority('BAN_PUBLICATION_AUTHORITY')")
    public ResponseEntity<Void> banPublication(@PathVariable long publicationId,
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
    public ResponseEntity<Void> deletePublication(@PathVariable long publicationId,
                                                  @Parameter(hidden = true) Authentication authentication) {
        String userNickname = authentication.getName();
        publicationService.deletePublication(userNickname, publicationId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
}
