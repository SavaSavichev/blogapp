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
    List<Post> findAllPostsByUserId (Integer userId);

    @Query("FROM Post p WHERE p.userId = ?1 AND p.isActive = 1 AND p.moderationStatus = 'ACCEPTED'")
    List<Post> findAllActivePostsByUserId(Integer userId);

    List<Post> findByTextContaining(String text);

    @Query("FROM Post p WHERE p.userId = ?1 AND p.isActive = 0 ORDER BY p.timestamp DESC")
    Page<Post> getInactivePosts(Integer userId, PageRequest pageRequest);

    @Query("FROM Post p WHERE p.userId = ?1 AND p.isActive = 1 AND p.moderationStatus = 'NEW' ORDER BY p.timestamp DESC")
    Page<Post> getPendingPosts(Integer userId, PageRequest pageRequest);

    @Query("FROM Post p WHERE p.userId = ?1 AND p.isActive = 1 AND p.moderationStatus = 'DECLINED' ORDER BY p.timestamp DESC")
    Page<Post> getDeclinedPosts(Integer userId, PageRequest pageRequest);

    @Query("FROM Post p WHERE p.userId = ?1 AND p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' ORDER BY p.timestamp DESC")
    Page<Post> getPublishedPosts(Integer userId, PageRequest pageRequest);

    @Query("FROM Post p WHERE p.moderatorId = ?1 AND p.isActive = 1 AND p.moderationStatus = 'NEW' ORDER BY p.timestamp DESC")
    Page<Post> getNewPostForModeration(Integer userId, PageRequest pageRequest);

    @Query("FROM Post p WHERE p.moderatorId = ?1 AND p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' ORDER BY p.timestamp DESC")
    Page<Post> getAcceptedPostForModeration(Integer userId, PageRequest pageRequest);

    @Query("FROM Post p WHERE p.moderatorId = ?1 AND p.isActive = 1 AND p.moderationStatus = 'DECLINED' ORDER BY p.timestamp DESC")
    Page<Post> getDeclinedPostsForModeration(Integer userId, PageRequest pageRequest);
}

