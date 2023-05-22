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
import ru.vsu.cs.picstorm.entity.User;
import ru.vsu.cs.picstorm.entity.UserRole;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestEntityManager
@Transactional
public class UserRepositoryTests {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TestEntityManager entityManager;

    private User user1;
    private User user2;
    private User user3;
    private User user4;
    private User user5;

    @PostConstruct
    private void initMockData() {
        user1 = new User(null, null, "aaaaa", "email1@email.com", "qwerty-hash", UserRole.ORDINARY, new ArrayList<>(), null);
        user2 = new User(null, null, "ddda", "email2@email.com", "qwerty-hash", UserRole.SUPER_ADMIN, new ArrayList<>(), null);
        user3 = new User(null, null, "bbbb", "email3@email.com", "qwerty-hash", UserRole.BANNED, new ArrayList<>(), null);
        user4 = new User(null, null, "neme", "email4@email.com", "qwerty-hash", UserRole.ORDINARY, new ArrayList<>(), null);
        user5 = new User(null, null, "ccaaccc", "email5@email.com", "qwerty-hash", UserRole.ADMIN, new ArrayList<>(), null);
    }

    @BeforeEach
    public void persistMockData() {
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(user3);
        entityManager.persist(user4);
        entityManager.persist(user5);
    }

    @Test
    public void findPageByNicknameByNotExistingName() {
        Page<User> page = userRepository.findPageByNickname("--str--", PageRequest.of(0, 2));

        assertTrue(page.isEmpty());
        assertEquals(0, page.getTotalElements());
        assertEquals(0, page.getContent().size());
    }

    @Test
    public void findPageByNicknameOfBanedUser() {
        Page<User> page = userRepository.findPageByNickname("bbbb", PageRequest.of(0, 2));

        assertTrue(page.isEmpty());
        assertEquals(0, page.getTotalElements());
        assertEquals(0, page.getContent().size());
    }


    @Test
    public void findPageByNicknameWithSingleUser() {
        Page<User> page = userRepository.findPageByNickname("nem", PageRequest.of(0, 2));

        assertEquals(1, page.getTotalElements());
        assertEquals(user4, page.getContent().get(0));
    }

    @Test
    public void findPageByNicknameWithMultipleUsers() {
        Page<User> page = userRepository.findPageByNickname("a", PageRequest.of(0, 2));

        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getContent().size());
        assertEquals(user1, page.getContent().get(0));
        assertEquals(user5, page.getContent().get(1));
    }
}
