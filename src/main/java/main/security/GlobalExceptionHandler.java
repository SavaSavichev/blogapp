package main.security;

import main.api.response.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({AuthenticationException.class, MissingCsrfTokenException.class,
            InvalidCsrfTokenException.class, SessionAuthenticationException.class})
    public ResponseEntity<?> handleAuthenticationException(RuntimeException ex,
                                                           HttpServletRequest request,
                                                           HttpServletResponse response) {
        LoginResponse loginResponse = new LoginResponse();
        return ResponseEntity.ok(loginResponse);
    }
}
