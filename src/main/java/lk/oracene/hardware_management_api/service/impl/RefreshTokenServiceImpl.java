package lk.oracene.hardware_management_api.service.impl;

import lk.oracene.hardware_management_api.exception.TokenRefreshException;
import lk.oracene.hardware_management_api.model.RefreshToken;
import lk.oracene.hardware_management_api.model.User;
import lk.oracene.hardware_management_api.repository.RefreshTokenRepository;
import lk.oracene.hardware_management_api.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpiration));

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException("Refresh token has expired. Please log in again.");
        }
        return token;
    }

    @Override
    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
