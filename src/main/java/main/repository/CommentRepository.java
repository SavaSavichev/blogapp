package main.repository;

import main.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    @Query("SELECT count(pc) FROM Comment pc WHERE pc.post_id = ?1")
    int getCommentCountByPostId(int post_id);
}
