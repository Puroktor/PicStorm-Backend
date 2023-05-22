package ru.vsu.cs.picstorm.controller;

import jakarta.validation.ValidationException;
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
import ru.vsu.cs.picstorm.dto.response.PageDto;
import ru.vsu.cs.picstorm.dto.response.SubscriptionDto;
import ru.vsu.cs.picstorm.dto.response.UserLineDto;
import ru.vsu.cs.picstorm.service.SubscriptionService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class SubscriptionControllerTests {
    @MockBean
    private Authentication authentication;
    @MockBean
    private SubscriptionService subscriptionService;
    @Autowired
    private SubscriptionController subscriptionController;

    @Test
    public void getSubscribersWithValidParams() {
        int userId = 1, index = 0, size = 1;
        PageDto<UserLineDto> pageDto = new PageDto<>();
        when(subscriptionService.getSubscribers(null, userId, index, size)).thenReturn(pageDto);
        ResponseEntity<?> returned = subscriptionController.getSubscribers(userId, index, size, authentication);

        assertEquals(HttpStatus.OK, returned.getStatusCode());
        assertEquals(pageDto, returned.getBody());
        verify(subscriptionService, times(1)).getSubscribers(null, userId, index, size);
    }

    @Test
    public void getSubscribersWithInvalidValidIndex() {
        int userId = 1, index = -1, size = 1;
        assertThrows(ValidationException.class, () -> subscriptionController.getSubscribers(userId, index, size, authentication));
        verify(subscriptionService, times(0)).getSubscribers(anyString(), anyLong(), anyInt(), anyInt());
    }

    @Test
    public void getSubscribersWithInvalidValidSize() {
        int userId = 1, index = 1, size = 0;
        assertThrows(ValidationException.class, () -> subscriptionController.getSubscribers(userId, index, size, authentication));
        verify(subscriptionService, times(0)).getSubscribers(anyString(), anyLong(), anyInt(), anyInt());
    }

    @Test
    public void getSubscriptionsWithValidParams() {
        int userId = 1, index = 0, size = 1;
        PageDto<UserLineDto> pageDto = new PageDto<>();
        when(subscriptionService.getSubscriptions(null, userId, index, size)).thenReturn(pageDto);
        ResponseEntity<?> returned = subscriptionController.getSubscriptions(userId, index, size, authentication);

        assertEquals(HttpStatus.OK, returned.getStatusCode());
        assertEquals(pageDto, returned.getBody());
        verify(subscriptionService, times(1)).getSubscriptions(null, userId, index, size);
    }

    @Test
    public void getSubscriptionsWithInvalidValidIndex() {
        int userId = 1, index = -1, size = 1;
        assertThrows(ValidationException.class, () -> subscriptionController.getSubscriptions(userId, index, size, authentication));
        verify(subscriptionService, times(0)).getSubscriptions(anyString(), anyLong(), anyInt(), anyInt());
    }

    @Test
    public void getSubscriptionsWithInvalidValidSize() {
        int userId = 1, index = 1, size = 0;
        assertThrows(ValidationException.class, () -> subscriptionController.getSubscriptions(userId, index, size, authentication));
        verify(subscriptionService, times(0)).getSubscriptions(anyString(), anyLong(), anyInt(), anyInt());
    }

    @Test
    @WithMockUser(authorities = "SUBSCRIBE_AUTHORITY")
    public void changeSubscriptionAsAuthorized() {
        int userId = 1;
        String username = "username";
        SubscriptionDto subscriptionDto = new SubscriptionDto();
        when(subscriptionService.changeSubscription(username, userId)).thenReturn(subscriptionDto);
        when(authentication.getName()).thenReturn(username);
        ResponseEntity<?> returned = subscriptionController.changeSubscription(1, authentication);

        assertEquals(HttpStatus.OK, returned.getStatusCode());
        assertEquals(subscriptionDto, returned.getBody());
        verify(subscriptionService, times(1)).changeSubscription(username, userId);
    }

    @Test
    public void changeSubscriptionAsUnauthorized() {
        assertThrows(AuthenticationException.class, () -> subscriptionController.changeSubscription(1, authentication));
        verify(subscriptionService, times(0)).changeSubscription(anyString(), anyLong());
    }

}
