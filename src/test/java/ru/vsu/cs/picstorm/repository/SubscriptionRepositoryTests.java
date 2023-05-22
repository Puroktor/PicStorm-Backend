package ru.vsu.cs.picstorm.repository;

import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.vsu.cs.picstorm.entity.Subscription;
import ru.vsu.cs.picstorm.entity.User;
import ru.vsu.cs.picstorm.entity.UserRole;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestEntityManager
@Transactional
public class SubscriptionRepositoryTests {

    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private TestEntityManager entityManager;

    private User user1;
    private User user2;
    private User user3;
    private User user4;
    private Subscription subscription1;
    private Subscription subscription2;
    private Subscription subscription3;
    private Subscription subscription4;

    @PostConstruct
    private void initMockData() {
        user1 = new User(null, null, "aaaaa", "email1@email.com", "hash", UserRole.ORDINARY, new ArrayList<>(), null);
        user2 = new User(null, null, "bbbb", "email2@email.com", "hash", UserRole.SUPER_ADMIN, new ArrayList<>(), null);
        user3 = new User(null, null, "cccc", "email3@email.com", "hash", UserRole.BANNED, new ArrayList<>(), null);
        user4 = new User(null, null, "ddddd", "email4@email.com", "hash", UserRole.ORDINARY, new ArrayList<>(), null);

        subscription1 = new Subscription(null, user1, user4, null);
        subscription2 = new Subscription(null, user1, user2, null);
        subscription3 = new Subscription(null, user1, user3, null);
        subscription4 = new Subscription(null, user3, user4, null);
    }

    @BeforeEach
    public void persistMockData() {
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(user3);
        entityManager.persist(user4);

        entityManager.persist(subscription1);
        entityManager.persist(subscription2);
        entityManager.persist(subscription3);
        entityManager.persist(subscription4);
    }

    @Test
    public void findPageBySubscriberWithTwoUsers() {
        Page<Subscription> page = subscriptionRepository.findPageBySubscriber(user1, PageRequest.of(0, 3));

        assertEquals(2, page.getTotalElements());
        assertEquals(2, page.getContent().size());
        assertEquals(subscription2, page.getContent().get(0));
        assertEquals(subscription1, page.getContent().get(1));
    }

    @Test
    public void findPageBySubscriberWithoutUsers() {
        Page<Subscription> page = subscriptionRepository.findPageBySubscriber(user2, PageRequest.of(0, 3));

        assertTrue(page.isEmpty());
        assertEquals(0, page.getTotalElements());
        assertEquals(0, page.getContent().size());
    }

    @Test
    public void findPageByTargetWithoutUsers() {
        Page<Subscription> page = subscriptionRepository.findPageByTarget(user1, PageRequest.of(0, 3));

        assertTrue(page.isEmpty());
        assertEquals(0, page.getTotalElements());
        assertEquals(0, page.getContent().size());
    }

    @Test
    public void findPageByTargetWithOneUser() {
        Page<Subscription> page = subscriptionRepository.findPageByTarget(user4, PageRequest.of(0, 3));

        assertEquals(1, page.getTotalElements());
        assertEquals(1, page.getContent().size());
        assertEquals(subscription1, page.getContent().get(0));
    }
}
