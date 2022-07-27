package main.service;

import lombok.AllArgsConstructor;
import main.api.response.GeneralResponse;
import main.model.Post;
import main.model.User;
import main.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@AllArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final VoteRepository voteRepository;
    private final CommentRepository commentRepository;

    public ResponseEntity<?> getPosts(Integer offset, Integer limit, String mode) {
        if (offset > limit) {
            return new ResponseEntity<>("Wrong input parameters!", HttpStatus.BAD_REQUEST);
        }

        List<Post> posts = getSortedPosts(offset, limit, mode);
        List<Map<String, Object>> postMapList = new ArrayList<>();

        for (Post post : posts) {
            Map<String, Object> responseMap = new LinkedHashMap<>();
            responseMap.put("id", post.getPostId());
            responseMap.put("timestamp", post.getTimestamp().getTime() / 1000);
            int userId = post.getUserId();
            User user = userRepository.getOne(userId);
            Map<String, Object> userMap = new LinkedHashMap<>();
            userMap.put("id", userId);
            userMap.put("name", user.getName());
            responseMap.put("user", userMap);
            responseMap.put("title", post.getTitle());
            responseMap.put("announce", post.getAnnounce().replaceAll("<(.*?)>", "").replaceAll("[\\p{P}\\p{S}]", ""));
            responseMap.put("likeCount", likeCount(post));
            responseMap.put("dislikeCount", dislikeCount(post));
            responseMap.put("commentCount", commentRepository.getCommentCountByPostId(post.getPostId()));
            responseMap.put("viewCount", post.getViewCount());
            postMapList.add(responseMap);
        }

        GeneralResponse generalResponse = new GeneralResponse();
        generalResponse.setCount(posts.size());
        generalResponse.setPosts(getOffsetLimit(postMapList, offset, limit));
        return ResponseEntity.ok(generalResponse);
    }

    private List<Post> getSortedPosts(Integer offset, Integer limit, String mode) {
        Page<Post> posts;
        PageRequest pageRequest = PageRequest.of(offset / limit, limit);
        switch (mode) {
            case "popular":
                posts = postRepository.getPopularPosts(pageRequest);
                break;
            case "best":
                posts = postRepository.getBestPosts(pageRequest);
                break;
            case "early":
                posts = postRepository.getEarlyPosts(pageRequest);
                break;
            default:
                posts = postRepository.getRecentPosts(pageRequest);
                break;
        }
        return posts.toList();
    }

    private Integer likeCount(Post post) {
        return voteRepository.findCountVotesByPostId(post.getPostId(), 1).orElse(0);
    }

    private Integer dislikeCount(Post post) {
        return voteRepository.findCountVotesByPostId(post.getPostId(), -1).orElse(0);
    }

    private List<Map<String, Object>> getOffsetLimit(List<Map<String, Object>> list,
                                                     Integer offset, Integer limit) {
        List<Map<String, Object>> listResult;
        if (offset > list.size() || offset > limit) {
            return new ArrayList<>();
        }
        if (limit + offset <= list.size()) {
            listResult = list.subList(offset, offset + limit);
        } else {
            listResult = list.subList(offset, list.size());
        }
        return listResult;
    }
}
