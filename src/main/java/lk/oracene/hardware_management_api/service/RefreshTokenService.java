package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.model.RefreshToken;
import lk.oracene.hardware_management_api.model.User;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(User user);
    RefreshToken verifyExpiration(RefreshToken token);
    void deleteByUser(User user);
}
