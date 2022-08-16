package main.controller;

import lombok.AllArgsConstructor;
import main.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class ApiAuthController
{
    private final AuthService authService;

//    public ApiAuthController(AuthService authService) {
//        this.authService = authService;
//    }

    @GetMapping("/check")
    private ResponseEntity<?> authResponse() {
        return ResponseEntity.ok(authService.getAuth());
    }
}
