package main.controller;

import lombok.AllArgsConstructor;
import main.api.request.PostModerationRequest;
import main.api.request.SettingsRequest;
import main.api.response.InitResponse;

import main.service.PostService;
import main.service.SettingsService;
import main.service.StatisticsService;
import main.service.TagService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class ApiGeneralController {

    private final InitResponse initResponse;
    private final TagService tagService;
    private final SettingsService settingsService;
    private final StatisticsService statisticService;
    private final PostService postService;

    @GetMapping("/init")
    public InitResponse init() {
        return initResponse;
    }

    @GetMapping("/settings")
    public ResponseEntity<?> getSettings() {
        return settingsService.getSettings();
    }

    @PutMapping("/settings")
    @PreAuthorize("hasAuthority('user:moderate')")
    public ResponseEntity<?> putSettings(@RequestBody @Valid SettingsRequest settings,
                                          Principal principal) {
        return settingsService.updateSettings(settings, principal);
    }

    @GetMapping("/tag/{query}")
    public ResponseEntity<?> getTag(@PathVariable("query") String query) {
        return tagService.getTag(query);
    }

    @GetMapping("/tag")
    public ResponseEntity<?> getTag() {
        return tagService.getTag();
    }

    @GetMapping("/statistics/my")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<?> getMyStatistics(Principal principal) {
        return statisticService.getMyStatistics(principal);
    }

    @GetMapping("/statistics/all")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<?> getAllStatistics(Principal principal) {
        return statisticService.getAllStatistics(principal);
    }

    @PostMapping("/moderation")
    @PreAuthorize("hasAuthority('user:moderate')")
    public ResponseEntity<?> moderation(@RequestBody PostModerationRequest postModerationRequest,
                                         Principal principal) {
        return postService.updateModeration(postModerationRequest.getId(),
                postModerationRequest.getDecision(), principal);
    }
}
