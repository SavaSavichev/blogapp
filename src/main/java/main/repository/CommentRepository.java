package main.repository;

import main.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    @Query("FROM Comment pc WHERE pc.post_id = ?1")
    List<Comment> findCommentsByPostId (int post_id);

    @Query("SELECT count(pc) FROM Comment pc WHERE pc.post_id = ?1")
    int getCommentCountByPostId(int post_id);
}
