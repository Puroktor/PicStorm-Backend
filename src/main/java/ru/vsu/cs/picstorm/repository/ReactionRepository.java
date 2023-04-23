package ru.vsu.cs.picstorm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vsu.cs.picstorm.entity.Publication;
import ru.vsu.cs.picstorm.entity.Reaction;
import ru.vsu.cs.picstorm.entity.User;

import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    Optional<Reaction> findByPublicationAndUser(Publication publication, User user);
}
