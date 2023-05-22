package ru.vsu.cs.picstorm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.vsu.cs.picstorm.entity.Subscription;
import ru.vsu.cs.picstorm.entity.User;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    @Query(value = "SELECT s FROM Subscription s WHERE s.subscriber = ?1 " +
            "AND s.target.role <> ru.vsu.cs.picstorm.entity.UserRole.BANNED ORDER BY s.target.nickname")
    Page<Subscription> findPageBySubscriber(User subscriber, Pageable pageable);
    @Query(value = "SELECT s FROM Subscription s WHERE s.target = ?1 " +
            "AND s.subscriber.role <> ru.vsu.cs.picstorm.entity.UserRole.BANNED ORDER BY s.subscriber.nickname")
    Page<Subscription> findPageByTarget(User target, Pageable pageable);
    Optional<Subscription> findBySubscriberAndTarget(User subscriber, User target);
    Long countBySubscriber(User subscriber);
    Long countByTarget(User target);
}
