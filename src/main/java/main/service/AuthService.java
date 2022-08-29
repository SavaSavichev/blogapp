package main.service;

import com.github.cage.Cage;
import com.github.cage.GCage;
import lombok.RequiredArgsConstructor;
import main.api.response.AuthResponse;
import main.dto.RegisterDTO;
import main.model.CaptchaCode;
import main.model.User;
import main.repository.CaptchaRepository;
import main.repository.UserRepository;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CaptchaRepository captchaRepository;
    private final UserRepository userRepository;
    private boolean result = false;

    public AuthResponse getAuth() {
        return new AuthResponse();
    }

    public ResponseEntity<?> registration(RegisterDTO registerDTO) {
        Optional<CaptchaCode> captchaCode = captchaRepository.findByCode(registerDTO.getCaptchaSecret());
        Optional<User> userOptional = userRepository.findOneByEmail(registerDTO.getEmail());
        Map<String, String> errors = new LinkedHashMap<>();
        Map<String, Object> regResult = new HashMap<>();
        User user = new User();

        if (userOptional.isPresent()) {
            errors.put("email", "Этот e-mail уже зарегистрирован!");
            result = false;
        }
        if (registerDTO.getName().length() > 25) {
            errors.put("name", "Ошибка: длина имени превышает 25 знаков!");
            result = false;
        }
        if (registerDTO.getPassword().length() < 6 || registerDTO.getPassword().length() > 12) {
            errors.put("password", "Пароль имеет недопустимую длину!");
            result = false;
        }
        if (captchaCode.isPresent()) {
            if(!registerDTO.getCaptcha().equals(captchaCode.get().getCode())) {
                errors.put("captcha", "Код с картинки введён неверно!");
                result = false;
            }
        }
        if (result) {
            String code = generateCode(16);
            user.setEmail(registerDTO.getEmail());
            user.setName(registerDTO.getName());
            user.setPassword(registerDTO.getPassword());
            user.setRegTime(Timestamp.valueOf(LocalDateTime.now()));
            user.setCode(code);
            user.setModerator(false);
            userRepository.save(user);
            regResult.put("result", Boolean.TRUE);
        }
        else{
            regResult.put("result", Boolean.FALSE);
            regResult.put("errors", errors);
        }
        return ResponseEntity.badRequest().body(regResult);
    }

    public ResponseEntity<?> getCaptcha() {
        Cage cage = new GCage();
        String secretCode = cage.getTokenGenerator().next();
        String code = cage.getTokenGenerator().next();
        String code64 = "";
        CaptchaCode captcha = new CaptchaCode();
        Map<String, String> map = new LinkedHashMap<>();
        try (OutputStream os = new FileOutputStream("image.png", false)) {
            cage.draw(code, os);
            byte[] fileContent = FileUtils.readFileToByteArray(new File("image.png"));
            code64 = Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        captcha.setCode(code);
        captcha.setSecretCode(secretCode);
        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
        captcha.setTimestamp(timestamp);
        captchaRepository.save(captcha);
        map.put("secret", secretCode);
        map.put("image", "data:image/png;base64, " + code64);
        deleteOldCaptchas();
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    private void deleteOldCaptchas() {
        List<CaptchaCode> oldCaptchas = captchaRepository.findAll().stream()
                .filter(c -> c.getTimestamp().getTime() < (Timestamp.valueOf(LocalDateTime.now()).getTime() - 3_600_000))
                .collect(Collectors.toList());
        captchaRepository.deleteInBatch(oldCaptchas);
    }

    private String generateCode(int length) {
        final String pattern = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            stringBuilder.append(pattern.charAt(rnd.nextInt(pattern.length())));
        }
        return stringBuilder.toString();
    }
}
