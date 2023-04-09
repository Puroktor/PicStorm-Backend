package ru.vsu.cs.picstorm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vsu.cs.picstorm.entity.User;
public interface UserRepository extends JpaRepository<User, Long> {
}
