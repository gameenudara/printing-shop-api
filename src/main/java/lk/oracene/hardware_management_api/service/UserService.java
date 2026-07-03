package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.dto.request.UpdateUserRequest;
import lk.oracene.hardware_management_api.dto.request.UserRequest;
import lk.oracene.hardware_management_api.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse createUser(UserRequest request);

    UserResponse updateUser(Long userId, UpdateUserRequest request);

    UserResponse getUserById(Long userId);

    Page<UserResponse> getAllUsers(Pageable pageable);

    Page<UserResponse> getActiveUsers(Pageable pageable);

    void deactivateUser(Long userId);

    UserResponse activateUser(Long userId);

    void resetPassword(Long userId, String newPassword);
}
