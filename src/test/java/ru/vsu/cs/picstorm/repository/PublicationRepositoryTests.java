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
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.vsu.cs.picstorm.dto.request.DateConstraint;
import ru.vsu.cs.picstorm.dto.request.UserConstraint;
import ru.vsu.cs.picstorm.entity.*;
import ru.vsu.cs.picstorm.repository.specification.PublicationFeedSpecification;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestEntityManager
@Transactional
public class PublicationRepositoryTests {
    @Autowired
    private PublicationRepository publicationRepository;
    @Autowired
    private TestEntityManager entityManager;

    private User user1;
    private User user2;
    private Subscription subscription;
    private Publication publication1;
    private Publication publication2;
    private Publication publication3;
    private Publication publication4;


    @PostConstruct
    private void initMockData() {
        user1 = new User(null, null, "aaaaa", "email1@email.com", "hash", UserRole.ORDINARY, new ArrayList<>(), null);
        user2 = new User(null, null, "bbbb", "email2@email.com", "hash", UserRole.SUPER_ADMIN, new ArrayList<>(), null);

        subscription = new Subscription(null, user1, user2, null);

        Picture picture1 = new Picture(null, PictureType.JPEG, null);
        Picture picture2 = new Picture(null, PictureType.PNG, null);
        Picture picture3 = new Picture(null, PictureType.JPEG, null);
        Picture picture4 = new Picture(null, PictureType.PNG, null);

        publication1 = new Publication(null, user1, picture1, PublicationState.VISIBLE, 0L, new ArrayList<>(), null);
        publication2 = new Publication(null, user2, picture2, PublicationState.VISIBLE, 1L, new ArrayList<>(), null);
        publication3 = new Publication(null, user2, picture3, PublicationState.VISIBLE, 0L, new ArrayList<>(), null);
        publication4 = new Publication(null, user2, picture4, PublicationState.BANNED, 0L, new ArrayList<>(), null);
    }

    @BeforeEach
    public void persistMockData() {
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(subscription);

        publication1 = entityManager.persist(publication1);
        publication2 = entityManager.persist(publication2);
        publication2.setCreated(Instant.now().minus(30, ChronoUnit.DAYS));
        publication2 = entityManager.persist(publication2);
        publication3 = entityManager.persist(publication3);
        publication4 = entityManager.persist(publication4);
    }

    @Test
    public void getCommonPublicationFeed() {
        PublicationFeedSpecification feedSpecification = new PublicationFeedSpecification(DateConstraint.NONE, UserConstraint.ALL, null, null);
        Page<Publication> page = publicationRepository.findAll(feedSpecification, PageRequest.of(0, 4));

        assertEquals(3, page.getTotalElements());
        assertEquals(3, page.getContent().size());
        assertEquals(publication3, page.getContent().get(0));
        assertEquals(publication1, page.getContent().get(1));
        assertEquals(publication2, page.getContent().get(2));
    }

    @Test
    public void getCommonPublicationFeedByWeek() {
        PublicationFeedSpecification feedSpecification = new PublicationFeedSpecification(DateConstraint.WEEK, UserConstraint.ALL, null, null);
        Page<Publication> page = publicationRepository.findAll(feedSpecification, PageRequest.of(0, 4));

        assertEquals(2, page.getTotalElements());
        assertEquals(2, page.getContent().size());
        assertEquals(publication3, page.getContent().get(0));
        assertEquals(publication1, page.getContent().get(1));
    }

    @Test
    public void getUserPublicationFeed() {
        PublicationFeedSpecification feedSpecification = new PublicationFeedSpecification(DateConstraint.NONE, UserConstraint.SPECIFIED, null, user1);
        Page<Publication> page = publicationRepository.findAll(feedSpecification, PageRequest.of(0, 4));

        assertEquals(1, page.getTotalElements());
        assertEquals(1, page.getContent().size());
        assertEquals(publication1, page.getContent().get(0));
    }

    @Test
    public void getSubscriptionPublicationFeedWithLikedFirst() {
        PublicationFeedSpecification feedSpecification = new PublicationFeedSpecification(DateConstraint.NONE, UserConstraint.SUBSCRIPTIONS, user1, null);
        Page<Publication> page = publicationRepository.findAll(feedSpecification, PageRequest.of(0, 4, Sort.by("rating").descending()));

        assertEquals(2, page.getTotalElements());
        assertEquals(2, page.getContent().size());
        assertEquals(publication2, page.getContent().get(0));
        assertEquals(publication3, page.getContent().get(1));
    }

    @Test
    public void getEmptySubscriptionPublicationFeed() {
        PublicationFeedSpecification feedSpecification = new PublicationFeedSpecification(DateConstraint.NONE, UserConstraint.SUBSCRIPTIONS, user2, null);
        Page<Publication> page = publicationRepository.findAll(feedSpecification, PageRequest.of(0, 4));

        assertTrue(page.isEmpty());
        assertEquals(0, page.getTotalElements());
        assertEquals(0, page.getContent().size());
    }

}
