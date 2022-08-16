package main.service;

import lombok.RequiredArgsConstructor;
import main.api.response.PostResponse;
import main.facade.PostFacade;
import main.model.Post;
import main.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;
    private final PostFacade postFacade;

    public ResponseEntity<?> getPosts(Integer offset, Integer limit, String mode) {
        if (offset > limit) {
            return new ResponseEntity<>("Wrong input parameters!", HttpStatus.BAD_REQUEST);
        }
        List<Post> posts = getSortedPosts(offset, limit, mode);

        PostResponse postResponse = postFacade.mappingPostResponse(posts, offset, limit);

        return ResponseEntity.ok(postResponse);
    }

    public List<Post> getSortedPosts(Integer offset, Integer limit, String mode) {
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

    public ResponseEntity<?> searchPosts(String query, Integer offset, Integer limit) {
        if (offset > limit) {
            return new ResponseEntity<>("Wrong input parameters!", HttpStatus.BAD_REQUEST);
        }
        List<Post> posts = postRepository.findByTextContaining(query.trim());

        PostResponse postResponse = postFacade.mappingPostSearchResponse(posts, offset, limit);
        return ResponseEntity.ok(postResponse);
    }
}
