package main.controller;

import lombok.AllArgsConstructor;
import main.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api")
@AllArgsConstructor
public class ApiPostController
{
    private final PostService postService;

    @GetMapping("/post")
    @ResponseBody
    public ResponseEntity<?> getPosts (@RequestParam(defaultValue="0") Integer offset,
                                       @RequestParam(defaultValue="10") Integer limit,
                                       @RequestParam String mode){
        return postService.getPosts(offset, limit, mode);
    }

    @GetMapping("/post/search")
    public ResponseEntity<?> searchPosts (@RequestParam String query,
                                         @RequestParam(defaultValue = "0") Integer offset,
                                         @RequestParam(defaultValue = "5") Integer limit){
        if (query.isBlank()) return getPosts(offset, limit, "recent");

        return postService.searchPosts(query, offset, limit);
    }
}
