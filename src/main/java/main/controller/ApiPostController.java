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

    @GetMapping("/calendar")
    public ResponseEntity<?> calendarPosts(@RequestParam Integer year) {
        return postService.calendarPosts(year);
    }


    @GetMapping("/post/byDate")
    public ResponseEntity<?> postsByDate(@RequestParam String date,
                                         @RequestParam(defaultValue = "0") Integer offset,
                                         @RequestParam(defaultValue = "5") Integer limit){
        return postService.postsByDate(date, offset, limit);
    }

    @GetMapping("/post/byTag")
    public ResponseEntity<?> postByTag(@RequestParam String tag,
                                       @RequestParam(defaultValue = "0") Integer offset,
                                       @RequestParam(defaultValue = "5") Integer limit){
        return postService.postsByTag(tag, offset, limit);
    }

    @GetMapping("/post/{ID:\\d+}")
    public ResponseEntity<?> postById(@PathVariable("ID") Integer ID) {
        return postService.getPostById(ID);
    }
}
