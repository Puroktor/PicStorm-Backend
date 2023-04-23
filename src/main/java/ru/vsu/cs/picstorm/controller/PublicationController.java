package ru.vsu.cs.picstorm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.picstorm.dto.request.PublicationReactionDto;
import ru.vsu.cs.picstorm.dto.request.UploadPictureDto;
import ru.vsu.cs.picstorm.dto.response.ResponsePictureDto;
import ru.vsu.cs.picstorm.service.PublicationService;

@RestController
@RequestMapping("/api/publication/")
@RequiredArgsConstructor
@Validated
@Tag(name = "Publication API", description = "Allows to upload, view, ban and delete publications")
@SecurityRequirement(name = "JWT Authentication")
public class PublicationController {
    private final PublicationService publicationService;

    @Operation(summary = "Uploads new publication")
    @PostMapping
    @PreAuthorize("hasAuthority('UPLOAD_AUTHORITY')")
    public ResponseEntity<Void> uploadPublication(
            @ModelAttribute @NotNull(message = "Предоставьте фото для загурзки") @Valid UploadPictureDto uploadPictureDto,
            @Parameter(hidden = true) Authentication authentication) {
        String userNickname = authentication.getName();
        publicationService.uploadPublication(userNickname, uploadPictureDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @Operation(summary = "Returns publication picture by id")
    @GetMapping("{publicationId}/picture")
    public ResponseEntity<ResponsePictureDto> getPublicationPicture(@PathVariable long publicationId) {
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
    @PreAuthorize("hasAuthority('BAN_USER_AUTHORITY')")
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
