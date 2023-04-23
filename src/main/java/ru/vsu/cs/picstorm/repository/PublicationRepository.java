package ru.vsu.cs.picstorm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.vsu.cs.picstorm.entity.Publication;
import ru.vsu.cs.picstorm.entity.User;

import java.util.List;

public interface PublicationRepository extends JpaRepository<Publication, Long>, JpaSpecificationExecutor<Publication> {
    List<Publication> findAllByOwner(User owner);
}
