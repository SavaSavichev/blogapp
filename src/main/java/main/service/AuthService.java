package main.service;

import com.github.cage.Cage;
import com.github.cage.GCage;
import lombok.RequiredArgsConstructor;
import main.api.request.LoginRequest;
import main.api.response.LoginResponse;
import main.dto.RegisterDTO;
import main.dto.UserCheckDTO;
import main.model.CaptchaCode;
import main.model.User;
import main.repository.CaptchaRepository;
import main.repository.UserRepository;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final AuthenticationManager authenticationManager;

    private boolean result;
    @Value("${config.nameLength}")
    private Integer nameLength;
    @Value("${config.passwordMinLength}")
    private Integer passMinLength;
    @Value("${config.passwordMaxLength}")
    private Integer passMaxLength;

    public ResponseEntity<?> registration(RegisterDTO registerDTO) {
        Optional<CaptchaCode> captchaCode = captchaRepository.findBySecretCode(registerDTO.getCaptchaSecret());
        Optional<User> userOptional = userRepository.findOneByEmail(registerDTO.getEmail());
        Map<String, String> errors = new LinkedHashMap<>();
        Map<String, Object> regResult = new HashMap<>();
        User user = new User();

        result = true;
        if (userOptional.isPresent()) {
            errors.put("email", "Этот e-mail уже зарегистрирован!");
            result = false;
        }
        if (registerDTO.getName().length() > nameLength) {
            errors.put("name", "Ошибка: длина имени превышает 25 знаков!");
            result = false;
        }
        if (registerDTO.getPassword().length() < passMinLength || registerDTO.getPassword().length() > passMaxLength) {
            errors.put("password", "Пароль имеет недопустимую длину!");
            result = false;
        }
        if(captchaCode.isPresent()) {
            if(!registerDTO.getCaptcha().equals(captchaCode.get().getCode())) {
                errors.put("captcha", "Код с картинки введён неверно!");
                result = false;
            }
        }
        if (result) {
            String code = generateCode(16);
            user.setEmail(registerDTO.getEmail())
                    .setName(registerDTO.getName())
                    .setPassword(registerDTO.getPassword())
                    .setRegTime(Timestamp.valueOf(LocalDateTime.now()))
                    .setCode(code)
                    .setIsModerator(0);
            userRepository.save(user);
            regResult.put("result", Boolean.TRUE);
            return ResponseEntity.ok(regResult);
        } else {
            regResult.put("result", Boolean.FALSE);
            regResult.put("errors", errors);
            return ResponseEntity.badRequest().body(regResult);
        }
    }

    public ResponseEntity<?> getLogin(LoginRequest loginRequest) {
        LoginResponse loginResponse = new LoginResponse();
        Optional<User> user1 = userRepository.findOneByEmail(loginRequest.getEmail());

        if (user1.isEmpty()) {
            return ResponseEntity.ok(loginResponse);
        }
        User currentUser = user1.get();
            Authentication auth =
                    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(auth);

            UserCheckDTO userCheckDTO = new UserCheckDTO();
            loginResponse.setResult(true);

        int modCount =
                currentUser.getIsModerator() == 1 ? userRepository.getModerationCount(currentUser.getUserId()) : 0;
        userCheckDTO.setId(currentUser.getUserId())
                    .setName(currentUser.getName())
                    .setPhoto(currentUser.getPhoto())
                    .setEmail(currentUser.getEmail())
                    .setModeration(currentUser.getIsModerator() == 1)
                    .setModerationCount(modCount)
                    .setSettings(currentUser.getIsModerator() == 1);
            loginResponse.setUser(userCheckDTO);
            return ResponseEntity.ok(loginResponse);
    }

    public ResponseEntity<?> getAuthCheck(String email) {
        User currentUser = userRepository.findOneByEmail(email).orElse(null);
        LoginResponse loginResponse = new LoginResponse();
        UserCheckDTO userCheckDTO = new UserCheckDTO();
        loginResponse.setResult(true);

        int modCount =
                currentUser.getIsModerator() == 1 ? userRepository.getModerationCount(currentUser.getUserId()) : 0;
        userCheckDTO.setId(currentUser.getUserId())
                .setName(currentUser.getName())
                .setPhoto(currentUser.getPhoto())
                .setEmail(currentUser.getEmail())
                .setModeration(currentUser.getIsModerator() == 1)
                .setModerationCount(modCount)
                .setSettings(currentUser.getIsModerator() == 1);
        loginResponse.setUser(userCheckDTO);
        return ResponseEntity.ok(loginResponse);
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
