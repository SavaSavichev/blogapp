package main.service;

import lombok.RequiredArgsConstructor;
import main.api.response.StatisticsResponse;
import main.model.GlobalSettings;
import main.model.Post;
import main.model.User;
import main.repository.GlobalSettingsRepository;
import main.repository.PostRepository;
import main.repository.UserRepository;
import main.repository.VoteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class StatisticsService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final VoteRepository voteRepository;
    private final GlobalSettingsRepository globalSettingsRepository;


    public ResponseEntity<?> getMyStatistics(Principal principal) {
        User user = userRepository.findOneByEmail(principal.getName()).orElse(null);
        assert user != null;
        Integer id = user.getUserId();
        StatisticsResponse statisticsResponse = new StatisticsResponse();

        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            int postsCount = postRepository.findAllActivePostsByUserId(id).size();
            if(postsCount == 0) {
                statisticsResponse.setPostsCount(0)
                        .setLikesCount(0)
                        .setDislikesCount(0)
                        .setViewsCount(0)
                        .setFirstPublication((long)0);
                return ResponseEntity.ok(statisticsResponse);
            }
            int postsLikeCount = (int) voteRepository.findAllPostVotesByUserId(id).stream().
                    filter(pv -> pv.getValue() == 1).
                    count();
            int postsDislikeCount = (int) voteRepository.findAllPostVotesByUserId(id).stream().
                    filter(pv -> pv.getValue() == -1).
                    count();
            int viewPostsCount = voteRepository.findAllPostVotesByUserId(id).size();
            List<Timestamp> localDates = postRepository.findAllPostsByUserId(id).stream().
                    map(Post::getTimestamp).collect(Collectors.toList());
            Timestamp minLocalDate = localDates.stream()
                    .min(Comparator.naturalOrder()).get();

            statisticsResponse.setPostsCount(postsCount)
                    .setLikesCount(postsLikeCount)
                    .setDislikesCount(postsDislikeCount)
                    .setViewsCount(viewPostsCount)
                    .setFirstPublication(minLocalDate.getTime() / 1000);
        }
        return ResponseEntity.ok(statisticsResponse);
    }

    public ResponseEntity<?> getAllStatistics(Principal principal) {
        StatisticsResponse statisticsResponse = new StatisticsResponse();
        List<Post> postList = (List<Post>) postRepository.findAllActivePosts();
        int postCount = postList.size();
        int likeCount = (int) voteRepository.findAll().stream().filter(p -> p.getValue() == 1)
                .count();
        int disLikeCount = (int) voteRepository.findAll().stream().filter(p -> p.getValue() == -1)
                .count();
        List<Integer> list = postRepository.findAll().stream().
                map(Post::getViewCount).
                collect(Collectors.toList());
        int viewCount = list.stream().
                reduce(Integer::sum).orElse(0);

        List<Timestamp> localDates = postRepository.findAll().stream().
                map(Post::getTimestamp).collect(Collectors.toList());
        Timestamp minLocalDate = localDates.stream()
                .min(Comparator.naturalOrder()).get();

        statisticsResponse.setPostsCount(postCount)
                .setLikesCount(likeCount)
                .setDislikesCount(disLikeCount)
                .setViewsCount(viewCount)
                .setFirstPublication(minLocalDate.getTime() / 1000);

        if (globalSettingsRepository.findAll().stream().
                findAny().
                orElse(new GlobalSettings()).
                isStatisticsIsPublic()) {
            return ResponseEntity.ok(statisticsResponse);
        } else {
            User user = userRepository.findOneByEmail(principal.getName()).orElse(null);

            assert user != null;
            if (user.getIsModerator() == 1) {
                return ResponseEntity.ok(statisticsResponse);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Доступ запрещен!");
            }
        }
    }
}
