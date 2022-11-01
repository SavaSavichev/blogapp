package main.service;

import com.github.cage.Cage;
import com.github.cage.GCage;
import lombok.RequiredArgsConstructor;
import main.api.request.LoginRequest;
import main.api.request.PasswordRequest;
import main.api.request.RegisterRequest;
import main.api.request.RestoreRequest;
import main.api.response.CaptchaResponse;
import main.api.response.LoginResponse;
import main.api.response.ResultResponse;
import main.config.SecurityConfig;

import main.dto.UserDTO;
import main.model.CaptchaCode;
import main.model.User;
import main.repository.CaptchaRepository;
import main.repository.UserRepository;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
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
    private final MailSender mailSender;
    private final SecurityConfig securityConfig;

    private boolean result;
    @Value("${config.nameLength}")
    private Integer nameLength;
    @Value("${config.passwordMinLength}")
    private Integer passMinLength;
    @Value("${config.passwordMaxLength}")
    private Integer passMaxLength;

    public ResponseEntity<?> registration(RegisterRequest registerRequest) {
        Optional<CaptchaCode> captchaCode = captchaRepository.findBySecretCode(registerRequest.getCaptchaSecret());
        Optional<User> userOptional = userRepository.findOneByEmail(registerRequest.getEmail());
        Map<String, String> errors = new LinkedHashMap<>();
        ResultResponse resultResponse = new ResultResponse();
        User user = new User();

        result = true;

        if (userOptional.isPresent()) {
            errors.put("email", "Этот e-mail уже зарегистрирован!");
            result = false;
        }
        if (registerRequest.getName().length() > nameLength) {
            errors.put("name", "Длина имени превышает допустимую длину!");
            result = false;
        }
        if (registerRequest.getPassword().length() < passMinLength || registerRequest.getPassword().length() > passMaxLength) {
            errors.put("password", "Пароль имеет недопустимую длину!");
            result = false;
        }
        if (captchaCode.isPresent()) {
            if (!registerRequest.getCaptcha().equals(captchaCode.get().getCode())) {
                errors.put("captcha", "Код с картинки введён неверно!");
                result = false;
            }
        }
        if (result) {
            String code = generateCode(16);
            user.setEmail(registerRequest.getEmail())
                    .setName(registerRequest.getName())
                    .setPassword(securityConfig.passwordEncoder().encode(registerRequest.getPassword()))
                    .setRegTime(Timestamp.valueOf(LocalDateTime.now()))
                    .setCode(code)
                    .setIsModerator(0);
            userRepository.save(user);
            resultResponse.setResult(true);
            return ResponseEntity.ok(resultResponse);
        } else {
            resultResponse.setResult(false);
            resultResponse.setErrors(errors);
            return ResponseEntity.badRequest().body(resultResponse);
        }
    }

    public ResponseEntity<?> getLogin(LoginRequest loginRequest) {
        LoginResponse loginResponse = new LoginResponse();
        Optional<User> user = userRepository.findOneByEmail(loginRequest.getEmail());

        if (user.isEmpty()) {
            return ResponseEntity.ok(loginResponse);
        }
        User currentUser = user.get();
        Authentication auth =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);
        loginResponse.setResult(true);
        loginResponse.setUser(userCheckDTO(currentUser));
        return ResponseEntity.ok(loginResponse);
    }

    public ResponseEntity<?> getAuthCheck(String email) {
        User currentUser = userRepository.findOneByEmail(email).orElse(null);
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setResult(true);

        assert currentUser != null;
        loginResponse.setUser(userCheckDTO(currentUser));
        return ResponseEntity.ok(loginResponse);
    }

    public UserDTO userCheckDTO(User user) {
        UserDTO userCheckDTO = new UserDTO();
        int modCount =
                user.getIsModerator() == 1 ? userRepository.getModerationCount(user.getUserId()) : 0;
        userCheckDTO.setId(user.getUserId())
                .setName(user.getName())
                .setPhoto(user.getPhoto())
                .setEmail(user.getEmail())
                .setModeration(user.getIsModerator() == 1)
                .setModerationCount(modCount)
                .setSettings(user.getIsModerator() == 1);

        return userCheckDTO;
    }

    public ResponseEntity<?> getCaptcha() {
        Cage cage = new GCage();
        String secretCode = cage.getTokenGenerator().next();
        String code = cage.getTokenGenerator().next();
        String code64 = "";
        CaptchaCode captcha = new CaptchaCode();
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
        CaptchaResponse captchaResponse = new CaptchaResponse();
        captchaResponse.setSecret(secretCode);
        captchaResponse.setImage("data:image/png;base64, " + code64);
        deleteOldCaptchas();
        return ResponseEntity.ok(captchaResponse);
    }

    private void deleteOldCaptchas() {
        List<CaptchaCode> oldCaptchas = captchaRepository.findAll().stream()
                .filter(c -> c.getTimestamp().getTime() < (Timestamp.valueOf(LocalDateTime.now()).getTime() - 3_600_000))
                .collect(Collectors.toList());
        captchaRepository.deleteInBatch(oldCaptchas);
    }

    private String generateCode(int length) {
        final String pattern = "0123456789abcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            stringBuilder.append(pattern.charAt(rnd.nextInt(pattern.length())));
        }
        return stringBuilder.toString();
    }

    public ResponseEntity<?> restore(RestoreRequest restoreRequest) {
        ResultResponse resultResponse = new ResultResponse();
        resultResponse.setResult(false);
        Optional<User> optionalUser = userRepository.findOneByEmail(restoreRequest.getEmail());
        if (optionalUser.isPresent()) {
            String code = generateCode(16);
            User user = optionalUser.get();
            user.setCode(code);
            userRepository.save(user);
            String text = "http://localhost:8080/login/change-password/" + code + "/";

            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setFrom("a.savichev13@gmail.com");
            simpleMailMessage.setTo(restoreRequest.getEmail());
            simpleMailMessage.setSubject("Восстановление пароля DevPub");
            simpleMailMessage.setText(text);
            try {
                mailSender.send(simpleMailMessage);
            } catch (MailSendException ex) {
                ex.printStackTrace();
                return ResponseEntity.badRequest().body(resultResponse);
            }
            resultResponse.setResult(true);
            return ResponseEntity.ok(resultResponse);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultResponse);
    }

    public ResponseEntity<?> changePassword(PasswordRequest password) {
        Optional<CaptchaCode> captchaSecret = captchaRepository.findBySecretCode(password.getCaptchaSecret());
        Optional<User> currentUser = userRepository.findOneByCode(password.getCode());
        ResultResponse resultResponse = new ResultResponse();
        Map<String, String> errors = new LinkedHashMap<>();
        result = true;

        if (currentUser.isPresent()) {
            if (password.getPassword().length() < passMinLength || password.getPassword().length() > passMaxLength) {
                errors.put("password", "Пароль имеет недопустимую длину!");
                result = false;
            }
            if (captchaSecret.isPresent()) {
                if (!password.getCaptcha().equals(captchaSecret.get().getCode())) {
                    errors.put("captcha", "Код с картинки введён неверно!");
                    result = false;
                }
            }
        } else {
            errors.put("code", "Ссылка для восстановления пароля не найдена!");
            result = false;
        }
        if (result) {
            User user = currentUser.get();
            user.setPassword(securityConfig.passwordEncoder().encode(password.getPassword()));
            userRepository.save(user);
            resultResponse.setResult(true);
        } else {
            resultResponse.setResult(false);
            resultResponse.setErrors(errors);
        }
        return ResponseEntity.ok(resultResponse);
    }
}
