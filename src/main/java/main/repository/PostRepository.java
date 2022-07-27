package main.repository;

import main.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    @Query("FROM Post p ORDER BY p.timestamp DESC")
    Page<Post> getRecentPosts (PageRequest pageRequest);

    @Query ("FROM Post p ORDER BY SIZE(p.postComments) DESC")
    Page<Post> getPopularPosts(PageRequest pageRequest);

    @Query("FROM Post p WHERE p.isActive = 1 ORDER BY p.postLikes.size DESC")
    Page<Post> getBestPosts(PageRequest pageRequest);

    @Query ("FROM Post p ORDER BY p.timestamp")
    Page<Post> getEarlyPosts(PageRequest pageRequest);

    @Query("FROM Post p WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED'")
    Collection<Post> findAllActivePosts ();
}

