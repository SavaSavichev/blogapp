package main.controller;

import lombok.RequiredArgsConstructor;
import main.api.request.ProfileRequest;
import main.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ApiProfileController {
    private final UserService userService;

    @PostMapping(value = "/my", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateProfile(@RequestBody ProfileRequest request, Principal principal) throws IOException {
        return userService.getPostProfileMy(request.getEmail(), request.getName(),
                request.getPassword(), request.getRemovePhoto(), principal);
    }

    @PostMapping(value = "/my", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateProfileWithPhoto(
            @RequestParam("photo") MultipartFile photo,
            @RequestParam("removePhoto") String removePhoto,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam(name = "password", required = false) String password,
            Principal principal
    ) throws IOException {
        return userService.getPostProfileMyWithPhoto(photo, email, name, password, removePhoto, principal);
    }
}