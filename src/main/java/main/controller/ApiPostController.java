package main.controller;

import main.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api")
public class ApiPostController
{
    private final PostService postService;

    public ApiPostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/post")
    @ResponseBody
    public ResponseEntity<?> getPosts (@RequestParam(defaultValue="0") Integer offset,
                                       @RequestParam(defaultValue="10") Integer limit,
                                       @RequestParam String mode){
        return postService.getPosts (offset, limit, mode);
    }
}
