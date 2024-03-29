package main.repository;

import main.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {
    @Query(value = "FROM Tag t WHERE t.name = ?1")
    List<Tag> findPostsByTagName(String name);

    Optional<Tag> findTagByName(String name);
}
