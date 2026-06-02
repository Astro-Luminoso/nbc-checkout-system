package dev.nbcsparta.assignment.nbccheckoutsystem.auth.controller;

import dev.nbcsparta.assignment.nbccheckoutsystem.auth.dto.SignupRequest;
import dev.nbcsparta.assignment.nbccheckoutsystem.auth.dto.SignupResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.auth.service.AuthService;
import dev.nbcsparta.assignment.nbccheckoutsystem.global.exception.CustomException;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        try {
            SignupResponse response = authService.signup(request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);
        } catch (CustomException exception) {
            return ResponseEntity
                    .status(exception.getStatus())
                    .body(Map.of("message", exception.getMessage()));
        }
    }
}
