package main.controller;

import lombok.AllArgsConstructor;
import main.api.request.LoginRequest;
import main.api.response.LoginResponse;
import main.dto.RegisterDTO;
import main.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class ApiAuthController
{
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest){
        return authService.getLogin(loginRequest);
    }

    @PostMapping("/register")
    private ResponseEntity<?> postAuthRegister(@RequestBody RegisterDTO registerDTO){
        return authService.registration(registerDTO);
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.getContext().setAuthentication(null);
        Map<String, Boolean> map = new HashMap<>();
        map.put("result", true);
        return ResponseEntity.ok(map);
    }

    @GetMapping("/check")
    private ResponseEntity<?> authResponse(Principal principal) {
        if (principal == null) return ResponseEntity.ok(new LoginResponse());
        return authService.getAuthCheck(principal.getName());
    }

    @GetMapping("/captcha")
    public ResponseEntity<?> getCaptcha() {
        return authService.getCaptcha();
    }
}
