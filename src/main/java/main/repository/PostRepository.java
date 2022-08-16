package main.repository;

import main.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    @Query("FROM Post p WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.timestamp <= now() ORDER BY p.timestamp DESC")
    Page<Post> getRecentPosts (PageRequest pageRequest);

    @Query ("FROM Post p WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.timestamp <= now() ORDER BY SIZE(p.postComments) DESC")
    Page<Post> getPopularPosts(PageRequest pageRequest);

    @Query("FROM Post p WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.timestamp <= now() ORDER BY p.postLikes.size DESC")
    Page<Post> getBestPosts(PageRequest pageRequest);

    @Query ("FROM Post p WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.timestamp <= now() ORDER BY p.timestamp")
    Page<Post> getEarlyPosts(PageRequest pageRequest);

    @Query("FROM Post p WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.timestamp <= now()")
    Collection<Post> findAllActivePosts ();

    @Query("FROM Post p WHERE p.userId = ?1")
    Collection<Post> findAllPostsByUserId (int userId);

    List<Post> findByTextContaining(String text);
}

