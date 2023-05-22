package ru.vsu.cs.picstorm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.vsu.cs.picstorm.entity.User;

import java.util.Optional;
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByNickname(String name);
    Optional<User> findByEmail(String email);
    @Query(value = "SELECT u FROM User u WHERE lower(u.nickname) LIKE lower(concat('%', ?1,'%'))" +
            "AND u.role <> ru.vsu.cs.picstorm.entity.UserRole.BANNED " +
            "ORDER BY u.nickname")
    Page<User> findPageByNickname(String nickname, Pageable pageable);
}
