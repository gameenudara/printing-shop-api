package lk.oracene.hardware_management_api.service.impl;

import lk.oracene.hardware_management_api.dto.request.UpdateUserRequest;
import lk.oracene.hardware_management_api.dto.request.UserRequest;
import lk.oracene.hardware_management_api.dto.response.UserResponse;
import lk.oracene.hardware_management_api.exception.BadRequestException;
import lk.oracene.hardware_management_api.exception.NotFoundException;
import lk.oracene.hardware_management_api.model.User;
import lk.oracene.hardware_management_api.repository.RefreshTokenRepository;
import lk.oracene.hardware_management_api.repository.UserRepository;
import lk.oracene.hardware_management_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already in use: " + request.getEmail());
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setIsActive(true);

        return mapToResponse(userRepository.save(user));
    }

    @Override
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());

        return mapToResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        return mapToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getActiveUsers(Pageable pageable) {
        return userRepository.findByIsActiveTrue(pageable).map(this::mapToResponse);
    }

    @Override
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadRequestException("User is already inactive");
        }
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    public UserResponse activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        if (Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadRequestException("User is already active");
        }
        user.setIsActive(true);
        return mapToResponse(userRepository.save(user));
    }

    @Override
    public void resetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        refreshTokenRepository.deleteByUser(user);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .createdBy(user.getCreatedBy())
                .updatedBy(user.getUpdatedBy())
                .build();
    }
}
