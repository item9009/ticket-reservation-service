package com.ticketing.reservation.application;

import com.ticketing.reservation.application.dto.LoginRequest;
import com.ticketing.reservation.application.dto.LoginResponse;
import com.ticketing.reservation.domain.entity.User;
import com.ticketing.reservation.domain.repository.UserRepository;
import com.ticketing.reservation.infrastructure.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getRole());
        log.info("로그인 성공: userId={}, role={}", user.getId(), user.getRole());

        return new LoginResponse(token, user.getId(), user.getRole());
    }

    public void signup(LoginRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = User.create(request.getEmail(),
                passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        log.info("회원가입 완료: email={}", request.getEmail());
    }
}
