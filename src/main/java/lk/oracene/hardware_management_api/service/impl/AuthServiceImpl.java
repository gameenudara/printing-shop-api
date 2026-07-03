package lk.oracene.hardware_management_api.service.impl;

import lk.oracene.hardware_management_api.dto.request.ChangePasswordRequest;
import lk.oracene.hardware_management_api.dto.request.LoginRequest;
import lk.oracene.hardware_management_api.dto.request.RefreshTokenRequest;
import lk.oracene.hardware_management_api.dto.response.AuthResponse;
import lk.oracene.hardware_management_api.exception.BadRequestException;
import lk.oracene.hardware_management_api.exception.NotFoundException;
import lk.oracene.hardware_management_api.exception.TokenRefreshException;
import lk.oracene.hardware_management_api.model.RefreshToken;
import lk.oracene.hardware_management_api.model.User;
import lk.oracene.hardware_management_api.repository.RefreshTokenRepository;
import lk.oracene.hardware_management_api.repository.UserRepository;
import lk.oracene.hardware_management_api.security.JwtUtil;
import lk.oracene.hardware_management_api.service.AuthService;
import lk.oracene.hardware_management_api.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        String accessToken = jwtUtil.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new TokenRefreshException("Refresh token not found. Please log in again."));

        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();
        String newAccessToken = jwtUtil.generateToken(user);

        return buildAuthResponse(user, newAccessToken, refreshToken.getToken());
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void logout() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        refreshTokenService.deleteByUser(user);
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .token(accessToken)
                .tokenType("Bearer")
                .userId(user.getUserId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .refreshToken(refreshToken)
                .build();
    }
}
