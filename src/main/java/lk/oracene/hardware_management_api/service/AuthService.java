package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.dto.request.ChangePasswordRequest;
import lk.oracene.hardware_management_api.dto.request.LoginRequest;
import lk.oracene.hardware_management_api.dto.request.RefreshTokenRequest;
import lk.oracene.hardware_management_api.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(RefreshTokenRequest request);
    void changePassword(ChangePasswordRequest request);
    void logout();
}
