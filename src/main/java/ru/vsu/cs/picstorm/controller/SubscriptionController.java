package ru.vsu.cs.picstorm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.picstorm.dto.response.PageDto;
import ru.vsu.cs.picstorm.dto.response.SubscriptionDto;
import ru.vsu.cs.picstorm.dto.response.UserLineDto;
import ru.vsu.cs.picstorm.service.SubscriptionService;

import java.util.Optional;

@RestController
@RequestMapping("/api/")
@RequiredArgsConstructor
@Validated
@Tag(name = "Subscriptions API", description = "Allows to subscribe to users, view subscriptions")
@SecurityRequirement(name = "JWT Authentication")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "Returns user subscribers by id")
    @GetMapping("subscribers/{userId}")
    public ResponseEntity<PageDto<UserLineDto>> getSubscribers(
            @Parameter(description = "User ID whose subscribers are required") @PathVariable("userId") long userId,
            @RequestParam("index") @Min(value = 0, message = "Индекс страницы должен быть >=0")
            @Parameter(description = "Index of desired page", example = "1") int index,
            @RequestParam("size") @Min(value = 1, message = "Размер страницы должен быть >=1")
            @Parameter(description = "Size of pages", example = "1") int size,
            @Parameter(hidden = true) Authentication authentication) {
        String viewingUserNickname = Optional.ofNullable(authentication).map(Authentication::getName).orElse(null);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(subscriptionService.getSubscribers(viewingUserNickname, userId, index, size));
    }

    @Operation(summary = "Returns user subscriptions by id")
    @GetMapping("subscriptions/{userId}")
    public ResponseEntity<PageDto<UserLineDto>> getSubscriptions(
            @Parameter(description = "User ID whose subscriptions are required") @PathVariable("userId") long userId,
            @RequestParam("index") @Min(value = 0, message = "Индекс страницы должен быть >=0")
            @Parameter(description = "Index of desired page", example = "1") int index,
            @RequestParam("size") @Min(value = 1, message = "Размер страницы должен быть >=1")
            @Parameter(description = "Size of pages", example = "1") int size,
            @Parameter(hidden = true) Authentication authentication) {
        String viewingUserNickname = Optional.ofNullable(authentication).map(Authentication::getName).orElse(null);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(subscriptionService.getSubscriptions(viewingUserNickname, userId, index, size));
    }

    @Operation(summary = "Subscribes/unsubscribes to user by id")
    @PutMapping("subscription/{userId}")
    @PreAuthorize("hasAuthority('SUBSCRIBE_AUTHORITY')")
    public ResponseEntity<SubscriptionDto> changeSubscription(
            @Parameter(description = "Target user ID") @PathVariable("userId") long userId,
            @Parameter(hidden = true) Authentication authentication) {
        String requesterUsername = authentication.getName();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(subscriptionService.changeSubscription(requesterUsername, userId));
    }
}
