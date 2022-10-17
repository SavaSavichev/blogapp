package main.service;

import lombok.RequiredArgsConstructor;
import main.api.response.ResultResponse;
import main.config.SecurityConfig;
import main.model.User;
import main.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SecurityConfig securityConfig;

    @Value("${config.passwordMinLength}")
    private Integer passwordMinLength;
    @Value("${config.passwordMaxLength}")
    private Integer passwordMaxLength;
    @Value("${config.avatarHeight}")
    private Integer avatarHeight;
    @Value("${config.avatarWidth}")
    private Integer avatarWidth;
    @Value("${config.nameLength}")
    private Integer nameLength;
    @Value("${config.maxImageSize}")
    private Integer maxImageSize;

    public ResponseEntity<?> updateProfileWithPhoto(MultipartFile photo, String email, String name,
                                                    String password, String removePhoto,
                                                    Principal principal) throws IOException {
        Optional<User> user = userRepository.findOneByEmail(principal.getName());
        User currentUser = user.get();
        Map<String, Object> errors = new LinkedHashMap<>();
        ResultResponse resultResponse = new ResultResponse();
        if (photo != null) {
            if (photo.getBytes().length <= maxImageSize) {
                File convertFile = saveImage(photo, avatarHeight, avatarWidth);
                String photoDestination = StringUtils.cleanPath(convertFile.getPath());
                currentUser.setPhoto("/" + photoDestination);
            } else {
                errors.put("photo", "Фото слишком большое, нужно не более 5 Мб.");
            }
        }
        errors.putAll(profileValidate(password, name));

        if (!errors.isEmpty()) {
            resultResponse.setResult(false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultResponse);
        } else {
            saveProfileChange(currentUser, removePhoto, email, password, name);
            resultResponse.setResult(true);
            return ResponseEntity.ok(resultResponse);
        }
    }

    public ResponseEntity<?> updateProfile(String email, String name,
                                           String password, String removePhoto,
                                           Principal principal) throws IOException {
        Optional<User> user = userRepository.findOneByEmail(principal.getName());
        User currentUser = user.get();
        ResultResponse resultResponse = new ResultResponse();
        Map<String, Object> errors = profileValidate(password, name);

        if (!errors.isEmpty()) {
            resultResponse.setResult(false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultResponse);
        } else {
            saveProfileChange(currentUser, removePhoto, email, password, name);
            resultResponse.setResult(true);
            return ResponseEntity.ok(resultResponse);
        }
    }

    private void saveProfileChange(User currentUser, String removePhoto, String email,
                                   String password, String name) {
        currentUser.setName(name);
        if (removePhoto != null) {
            if (removePhoto.equals("1")) {
                currentUser.setPhoto("");
            }
        }
        if (email != null && !currentUser.getEmail().equals(email)) {
            currentUser.setEmail(email);
        }
        if (password != null) {
            currentUser.setPassword(securityConfig.passwordEncoder().encode(password));
        }
        userRepository.save(currentUser);
    }

    private Map<String, Object> profileValidate(String password, String name) {
        Map<String, Object> errors = new LinkedHashMap<>();
        if (password != null) {
            if (password.length() < passwordMinLength || password.length() > passwordMaxLength) {
                errors.put("password", "Длина пароля с ошибкой");
            }
        }
        if (!name.matches("[a-zA-Z]*") || name.length() > nameLength || name.length() < 2) {
            errors.put("name", "Имя указано неверно.");
        }
        return errors;
    }

    public File saveImage(MultipartFile photo, int height, int width) throws IOException {
        String targetFolder = "upload/";
        String hashCode = String.valueOf(Math.abs(targetFolder.hashCode()));
        String folder1 = hashCode.substring(0, hashCode.length() / 3);
        String folder2 = hashCode.substring(1 + hashCode.length() / 3, 2 * hashCode.length() / 3);
        String folder3 = hashCode.substring(1 + 2 * hashCode.length() / 3);
        File destFolder = new File(targetFolder);
        if (!destFolder.exists()) {
            destFolder.mkdir();
        }
        File destFolder1 = new File(targetFolder + folder1);
        if (!destFolder1.exists()) {
            destFolder1.mkdir();
        }
        File destFolder2 = new File(targetFolder + folder1 + "/" + folder2);
        if (!destFolder2.exists()) {
            destFolder2.mkdir();
        }
        File destFolder3 = new File(targetFolder + folder1 + "/" + folder2 + "/" + folder3);
        if (!destFolder3.exists()) {
            destFolder3.mkdir();
        }
        int suffix = (int) (Math.random() * 100);
        String fileName = suffix + "_" + photo.getOriginalFilename();
        String finalDestination = targetFolder + folder1 + "/" + folder2 + "/" + folder3 + "/" + fileName;
        photo.transferTo(Path.of(finalDestination));
        File destFile = new File(finalDestination);
        Image image = ImageIO.read(photo.getInputStream());
        BufferedImage tempPNG = resizeImage(image, width, height);
        ImageIO.write(tempPNG, "png", destFile);
        return destFile;
    }

    private BufferedImage resizeImage(Image image, int width, int height) {
        final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setComposite(AlphaComposite.Src);
        graphics2D.drawImage(image, 0, 0, width, height, null);
        graphics2D.dispose();
        return bufferedImage;
    }

    public HttpEntity<byte[]> getPhoto(String folder, String dir1, String dir2, String dir3,
                                       String filename)
            throws IOException {
        String source = folder + "/" + dir1 + "/" + dir2 + "/" + dir3 + "/" + filename;
        byte[] image = org.apache.commons.io.FileUtils.readFileToByteArray(new File(source));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(image.length);
        return new HttpEntity<>(image, headers);
    }
}
