package main.controller;

import lombok.RequiredArgsConstructor;
import main.api.request.CommentRequest;
import main.api.request.LikeDislikeRequest;
import main.api.request.PostRequest;
import main.service.PostService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping(value = "/api")
@RequiredArgsConstructor
public class ApiPostController {

    private final PostService postService;

    @GetMapping("/post")
    @ResponseBody
    public ResponseEntity<?> getPosts(@RequestParam(defaultValue = "0") Integer offset,
                                      @RequestParam(defaultValue = "10") Integer limit,
                                      @RequestParam String mode) {
        return postService.getPosts(offset, limit, mode);
    }

    @PostMapping("/post")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<?> createPost(@RequestBody PostRequest postRequest, Principal principal) {
        return postService.createPost(postRequest, principal);
    }

    @PutMapping("/post/{ID:\\d+}")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<?> putPost(@RequestBody PostRequest postRequest,
                                     @PathVariable(value = "ID") int id) {
        return postService.changePost(id, postRequest);
    }

    @GetMapping("/post/search")
    public ResponseEntity<?> searchPosts(@RequestParam String query,
                                         @RequestParam(defaultValue = "0") Integer offset,
                                         @RequestParam(defaultValue = "5") Integer limit) {
        if (query.isBlank()) return getPosts(offset, limit, "recent");
        return postService.searchPosts(query, offset, limit);
    }

    @GetMapping("/calendar")
    public ResponseEntity<?> calendarPosts(@RequestParam Integer year) {
        return postService.calendarPosts(year);
    }


    @GetMapping("/post/byDate")
    public ResponseEntity<?> postsByDate(@RequestParam String date,
                                         @RequestParam(defaultValue = "0") Integer offset,
                                         @RequestParam(defaultValue = "5") Integer limit) {
        return postService.postsByDate(date, offset, limit);
    }

    @GetMapping("/post/byTag")
    public ResponseEntity<?> postByTag(@RequestParam String tag,
                                       @RequestParam(defaultValue = "0") Integer offset,
                                       @RequestParam(defaultValue = "5") Integer limit) {
        return postService.postsByTag(tag, offset, limit);
    }

    @GetMapping("/post/{ID:\\d+}")
    public ResponseEntity<?> postById(@PathVariable("ID") Integer id) {
        return postService.postById(id);
    }

    @GetMapping("/post/my")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<?> postByUser(Principal principal,
                                        @RequestParam(defaultValue = "0") Integer offset,
                                        @RequestParam(defaultValue = "5") Integer limit,
                                        @RequestParam String status) {
        return postService.myPosts(principal, offset, limit, status);
    }

    @GetMapping("/post/moderation")
    @PreAuthorize("hasAuthority('user:moderate')")
    public ResponseEntity<?> getPostsForModeration(@RequestParam(defaultValue = "0") Integer offset,
                                                   @RequestParam(defaultValue = "3") Integer limit,
                                                   @RequestParam(defaultValue = "NEW") String status,
                                                   Principal principal) {
        return postService.postsForModeration(offset, limit, status, principal);
    }

    @PostMapping("/comment")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<?> comment(@RequestBody CommentRequest commentRequest, Principal principal) {
        return postService.addComment(commentRequest, principal);
    }

    @PostMapping("/post/like")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<?> like(@RequestBody LikeDislikeRequest likeDislikeRequest, Principal principal) {
        return postService.addLikeDislike(likeDislikeRequest, principal, 1);
    }

    @PostMapping("/post/dislike")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<?> dislike(@RequestBody LikeDislikeRequest likeDislikeRequest, Principal principal) {
        return postService.addLikeDislike(likeDislikeRequest, principal, -1);
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<?> image(@RequestBody MultipartFile image) throws IOException {
        return postService.addImage(image);
    }
}
