package com.ticketing.reservation.interfaces.api;

import com.ticketing.reservation.application.dto.LoginRequest;
import com.ticketing.reservation.application.dto.LoginResponse;
import com.ticketing.reservation.application.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "인증 API", description = "로그인 · JWT 발급")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인 후 JWT 토큰을 발급합니다.")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "이메일/비밀번호로 회원가입합니다.")
    public ResponseEntity<Void> signup(@Valid @RequestBody LoginRequest request) {
        authService.signup(request);
        return ResponseEntity.ok().build();
    }
}
