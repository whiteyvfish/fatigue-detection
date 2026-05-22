package com.fatigue.detection.repository;

import com.fatigue.detection.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameAndDeleted(String username, Integer deleted);

    Optional<User> findByEmailAndDeleted(String email, Integer deleted);

    boolean existsByUsernameAndDeleted(String username, Integer deleted);

    boolean existsByEmailAndDeleted(String email, Integer deleted);
}
