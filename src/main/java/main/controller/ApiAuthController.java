package main.controller;

import lombok.AllArgsConstructor;
import main.api.request.LoginRequest;
import main.api.request.PasswordRequest;
import main.api.request.RegisterRequest;
import main.api.request.RestoreRequest;
import main.api.response.LoginResponse;
import main.api.response.ResultResponse;
import main.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class ApiAuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        return authService.getLogin(loginRequest);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        return authService.registration(registerRequest);
    }

    @GetMapping("/logout")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.getContext().setAuthentication(null);
        ResultResponse resultResponse = new ResultResponse();
        resultResponse.setResult(true);
        return ResponseEntity.ok(resultResponse);
    }

    @PostMapping("/restore")
    public ResponseEntity<?> restore(@RequestBody RestoreRequest restoreRequest) {
        return authService.restore(restoreRequest);
    }

    @PostMapping("/password")
    public ResponseEntity<?> authPassword(@RequestBody PasswordRequest password, Principal principal) {
        return authService.authPassword(password, principal);
    }

    @GetMapping("/check")
    public ResponseEntity<?> authCheck(Principal principal) {
        if (principal == null) return ResponseEntity.ok(new LoginResponse());
        return authService.getAuthCheck(principal.getName());
    }

    @GetMapping("/captcha")
    public ResponseEntity<?> getCaptcha() {
        return authService.getCaptcha();
    }
}
