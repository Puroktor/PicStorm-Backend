package ru.vsu.cs.picstorm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vsu.cs.picstorm.entity.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
}
