package main.repository;

import main.model.Tag2Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Tag2PostRepository extends JpaRepository<Tag2Post, Integer> {

    @Query("SELECT t2p.postId FROM Tag2Post t2p WHERE t2p.tagId = ?1")
    List<Integer> findPostIdByTagId(Integer tagId);

    @Query("SELECT tp.tagId FROM Tag2Post tp WHERE tp.postId = ?1")
    List<Integer> findTagIdsByPostId(int postId);
}
