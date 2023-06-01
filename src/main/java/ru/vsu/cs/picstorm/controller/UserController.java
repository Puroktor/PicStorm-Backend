package ru.vsu.cs.picstorm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.picstorm.dto.response.PageDto;
import ru.vsu.cs.picstorm.dto.response.UserLineDto;
import ru.vsu.cs.picstorm.dto.response.UserProfileDto;
import ru.vsu.cs.picstorm.dto.response.UserRoleDto;
import ru.vsu.cs.picstorm.service.UserService;

import java.util.Optional;

@RestController
@RequestMapping("/api/user/")
@RequiredArgsConstructor
@Validated
@Tag(name = "User API", description = "Allows to view profiles, search for users and change roles")
@SecurityRequirement(name = "JWT Authentication")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Uploads new avatar")
    @PostMapping("avatar")
    @PreAuthorize("hasAuthority('UPLOAD_AUTHORITY')")
    public ResponseEntity<Void> uploadAvatar(
            @RequestBody @NotNull(message = "Предоставьте фото для загурзки") @Valid MultipartFile uploadPicture,
            @Parameter(hidden = true) Authentication authentication) {
        String userNickname = authentication.getName();
        userService.uploadAvatar(userNickname, uploadPicture);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @Operation(summary = "Finds users by part of nickname")
    @GetMapping("search")
    public ResponseEntity<PageDto<UserLineDto>> findUsersByNickname(
            @Parameter(description = "Nickname part for search")
            @RequestParam(value = "nickname", required = false) String nickname,
            @RequestParam("index") @Min(value = 0, message = "Индекс страницы должен быть >=0")
            @Parameter(description = "Index of desired page", example = "1") int index,
            @RequestParam("size") @Min(value = 1, message = "Размер страницы должен быть >=1")
            @Parameter(description = "Size of pages") int size,
            @Parameter(hidden = true) Authentication authentication) {
        String searchingUserNickname = Optional.ofNullable(authentication).map(Authentication::getName).orElse(null);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.findUsersByNickname(searchingUserNickname, nickname, index, size));
    }

    @Operation(summary = "Returns user profile info")
    @GetMapping("{userId}/profile")
    public ResponseEntity<UserProfileDto> getUserProfile(
            @Parameter(description = "User ID whose profile is required") @PathVariable("userId") long userId,
            @Parameter(hidden = true) Authentication authentication) {
        String requesterUsername = Optional.ofNullable(authentication).map(Authentication::getName).orElse(null);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getUserProfile(requesterUsername, userId));
    }

    @Operation(summary = "Bans specified user")
    @PutMapping("{userId}/ban")
    @PreAuthorize("hasAuthority('BAN_USER_AUTHORITY')")
    public ResponseEntity<UserRoleDto> banUser(@Parameter(description = "User ID to be banned") @PathVariable("userId") long userId,
                                               @Parameter(hidden = true) Authentication authentication) {
        String requesterUsername = authentication.getName();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.banUser(requesterUsername, userId));
    }

    @Operation(summary = "Manages admin roles",
            description = "Gives the user admin role if he is not already, otherwise makes him an ordinary user")
    @PutMapping("{userId}/admin")
    @PreAuthorize("hasAuthority('MANAGE_ADMINS_AUTHORITY')")
    public ResponseEntity<UserRoleDto> changeAdminRole(
            @Parameter(description = "User ID whose admin role to be changed") @PathVariable("userId") long userId,
            @Parameter(hidden = true) Authentication authentication) {
        String requesterUsername = authentication.getName();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.changeAdminRole(requesterUsername, userId));
    }
}
