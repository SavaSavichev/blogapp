package main.controller;

import lombok.RequiredArgsConstructor;
import main.api.request.CommentRequest;
import main.api.request.LikeDislikeRequest;
import main.api.request.PostRequest;
import main.service.PostService;
import main.utils.ResponseErrorValidator;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping(value = "/api")
@RequiredArgsConstructor
public class ApiPostController {
    private final PostService postService;
    private final ResponseErrorValidator responseErrorValidator;

    @GetMapping("/post")
    @ResponseBody
    public ResponseEntity<?> getPosts (@RequestParam(defaultValue="0") Integer offset,
                                       @RequestParam(defaultValue="10") Integer limit,
                                       @RequestParam String mode) {
        return postService.getPosts(offset, limit, mode);
    }

    @PostMapping("/post")
    public ResponseEntity<?> createPost(@Valid @RequestBody PostRequest postRequest, BindingResult result, Principal principal) {
        ResponseEntity<Object> errors = responseErrorValidator.mapValidationService(result);
        if (!ObjectUtils.isEmpty(errors)) return errors;

        return postService.createPost(postRequest, principal);
    }

    @PutMapping("/post/{ID:\\d+}")
    public ResponseEntity<?> putPost (@Valid @RequestBody PostRequest postRequest, BindingResult result, @PathVariable(value = "ID") int id){
        ResponseEntity<Object> errors = responseErrorValidator.mapValidationService(result);
        if (!ObjectUtils.isEmpty(errors)) return errors;

        return postService.putPost(id, postRequest);
    }

    @GetMapping("/post/search")
    public ResponseEntity<?> searchPosts (@RequestParam String query,
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
    public ResponseEntity<?> postById(@PathVariable("ID") Integer ID) {
        return postService.getPostById(ID);
    }

    @GetMapping("/post/my")
    public ResponseEntity<?> postByUser(Principal principal,
                                        @RequestParam(defaultValue = "0") Integer offset,
                                        @RequestParam(defaultValue = "5") Integer limit,
                                        @RequestParam String status) {
        return postService.getMyPosts(principal, offset, limit, status);
    }

    @GetMapping("/post/moderation")
    public ResponseEntity<?> getPostsForModeration(@RequestParam(defaultValue="0") Integer offset,
                                                   @RequestParam(defaultValue="3") Integer limit,
                                                   @RequestParam(defaultValue="NEW") String status,
                                                   Principal principal) {
        return postService.getPostsForModeration(offset, limit, status, principal);
    }

    @PostMapping("/comment")
    private ResponseEntity<?> comment(@Valid @RequestBody CommentRequest commentRequest, BindingResult result, Principal principal) {
        ResponseEntity<Object> errors = responseErrorValidator.mapValidationService(result);
        if (!ObjectUtils.isEmpty(errors)) return errors;

        return postService.postComment(commentRequest, principal);
    }

    @PostMapping("/post/like")
    public ResponseEntity<?> like(@RequestBody LikeDislikeRequest likeDislikeRequest, Principal principal) {
        return postService.postLikeDislike(likeDislikeRequest, principal, 1);
    }

    @PostMapping("/post/dislike")
    public ResponseEntity<?> dislike(@RequestBody LikeDislikeRequest likeDislikeRequest, Principal principal)
    {
        return postService.postLikeDislike(likeDislikeRequest, principal, -1);
    }

    @PostMapping (value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public @ResponseBody ResponseEntity<?> image(@RequestBody MultipartFile image) throws IOException {
        return postService.postImage(image);
    }
}
