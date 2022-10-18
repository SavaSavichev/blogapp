package main.controller;

import lombok.RequiredArgsConstructor;
import main.service.UserService;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/{folder}/{dir1}/{dir2}/{dir3}/{filename}")
@RequiredArgsConstructor
public class ApiAvatarController {

    private final UserService userService;

    @GetMapping("")
    @ResponseBody
    public HttpEntity<byte[]> getPhoto(
            @PathVariable("folder") String folder,
            @PathVariable("dir1") String dir1,
            @PathVariable("dir2") String dir2,
            @PathVariable("dir3") String dir3,
            @PathVariable("filename") String filename) throws IOException {
        return userService.getPhoto(folder, dir1, dir2, dir3, filename);
    }
}
