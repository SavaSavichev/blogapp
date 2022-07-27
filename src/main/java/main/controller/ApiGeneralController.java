package main.controller;

import main.api.response.InitResponse;

import main.service.SettingsService;
import main.service.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiGeneralController
{
    private final InitResponse initResponse;
    private final TagService tagService;
    private final SettingsService settingsService;

    public ApiGeneralController(InitResponse initResponse, TagService tagService, SettingsService settingsService) {
        this.initResponse = initResponse;
        this.tagService = tagService;
        this.settingsService = settingsService;
    }

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
