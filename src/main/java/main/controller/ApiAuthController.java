package main.controller;

import lombok.AllArgsConstructor;
import main.dto.RegisterDTO;
import main.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class ApiAuthController
{
    private final AuthService authService;

    @PostMapping("/register")
    private ResponseEntity<?> postAuthRegister(@RequestBody RegisterDTO registerDTO){
        return authService.registration(registerDTO);
    }

    @GetMapping("/check")
    private ResponseEntity<?> authResponse() {
        return ResponseEntity.ok(authService.getAuth());
    }

    @GetMapping("/captcha")
    public ResponseEntity<?> getCaptcha() {
        return authService.getCaptcha();
    }
}
