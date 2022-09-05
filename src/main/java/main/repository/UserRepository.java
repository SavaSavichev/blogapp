package main.repository;

import main.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findOneByEmail(String email);

    @Query("SELECT count(p) FROM Post p WHERE p.moderationStatus = 'NEW' AND p.moderatorId = ?1")
    Integer getModerationCount(Integer userId);
}
