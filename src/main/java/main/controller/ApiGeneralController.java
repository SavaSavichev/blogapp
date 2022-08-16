package main.controller;

import lombok.AllArgsConstructor;
import main.api.response.InitResponse;

import main.service.PostService;
import main.service.SettingsService;
import main.service.TagService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class ApiGeneralController
{
    private final InitResponse initResponse;
    private final TagService tagService;
    private final SettingsService settingsService;
    private final PostService postService;

    @GetMapping("/init")
    private InitResponse init() {
        return initResponse;
    }

    @GetMapping("/settings")
    private ResponseEntity<?> getApiSettings () {
        return settingsService.getSettings();
    }

    @GetMapping("/tag")
    private ResponseEntity<?> getTag() {
        return tagService.getTag();
    }
}
