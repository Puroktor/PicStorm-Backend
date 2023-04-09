package ru.vsu.cs.picstorm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vsu.cs.picstorm.entity.Picture;

public interface PictureRepository extends JpaRepository<Picture, Long> {
}
