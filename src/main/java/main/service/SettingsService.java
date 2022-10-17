package main.service;

import lombok.RequiredArgsConstructor;
import main.api.request.SettingsRequest;
import main.api.response.ResultResponse;
import main.api.response.SettingsResponse;
import main.model.GlobalSettings;
import main.model.User;
import main.repository.GlobalSettingsRepository;
import main.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.Principal;

@RequiredArgsConstructor
@Service
public class SettingsService {

    private GlobalSettings globalSettings;
    private final GlobalSettingsRepository globalSettingsRepository;
    private final UserRepository userRepository;

    public ResponseEntity<?> getSettings() {
        SettingsResponse settings = new SettingsResponse();
        globalSettings = globalSettingsRepository.findAll().stream().findFirst().orElse(new GlobalSettings());

        settings.setMultiuserMode(globalSettings.isMultiuserMode())
                .setPostPremoderation(globalSettings.isPostPremoderation())
                .setStatisticsIsPublic(globalSettings.isStatisticsIsPublic());

        return ResponseEntity.ok(settings);
    }

    public ResponseEntity<?> updateSettings(SettingsRequest settingsRequest, Principal principal) {
        User user = userRepository.findOneByEmail(principal.getName()).orElse(null);
        assert user != null;
        Integer userId = user.getUserId();

        globalSettings = new GlobalSettings();
        if (userRepository.getOne(userId).getIsModerator() == 1) {
            globalSettingsRepository.deleteAll();
            globalSettings.setMultiuserMode(settingsRequest.isMultiuserMode())
                        .setPostPremoderation(settingsRequest.isPostPremoderation())
                        .setStatisticsIsPublic(settingsRequest.isStatisticsIsPublic());
            globalSettingsRepository.save(globalSettings);

            return getSettings();
        } else {
            ResultResponse resultResponse = new ResultResponse();
            resultResponse.setResult(false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultResponse);
        }
    }
}
