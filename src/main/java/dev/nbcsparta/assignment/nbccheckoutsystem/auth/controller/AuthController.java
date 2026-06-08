package dev.nbcsparta.assignment.nbccheckoutsystem.auth.controller;

import dev.nbcsparta.assignment.nbccheckoutsystem.auth.dto.LoginRequest;
import dev.nbcsparta.assignment.nbccheckoutsystem.auth.dto.LoginResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.auth.dto.SignupRequest;
import dev.nbcsparta.assignment.nbccheckoutsystem.auth.dto.SignupResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.auth.service.AuthService;
import dev.nbcsparta.assignment.nbccheckoutsystem.global.response.ApiResponse;
import jakarta.validation.Valid;

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
	public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
		SignupResponse response = authService.signup(request);
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(ApiResponse.success(response));
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
		LoginResponse response = authService.login(request);
		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
