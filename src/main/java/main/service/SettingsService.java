package main.service;

import lombok.RequiredArgsConstructor;
import main.model.GlobalSettings;
import main.repository.GlobalSettingsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class SettingsService {
    private GlobalSettings globalSettings;
    private final GlobalSettingsRepository globalSettingsRepository;

    public ResponseEntity<?> getSettings() {
        Map<String, Boolean> map = getBooleanMap();
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    private Map<String, Boolean> getBooleanMap() {
        Map<String, Boolean> map = new LinkedHashMap<>();
        globalSettings = globalSettingsRepository.findAll().stream().findFirst().orElse(new GlobalSettings());
        map.put("MULTIUSER_MODE", globalSettings.isMultiuserMode());
        map.put("POST_PREMODERATION", globalSettings.isPostPremoderation());
        map.put("STATISTICS_IS_PUBLIC", globalSettings.isStatisticsIsPublic());
        return map;
    }
}
