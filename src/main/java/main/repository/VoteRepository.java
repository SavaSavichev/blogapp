package main.repository;

import main.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Integer> {
    @Query("SELECT count(pv) FROM Vote pv WHERE pv.postId = ?1 AND pv.value = ?2")
    Optional<Integer> findCountVotesByPostId (int postId, int value);
}
