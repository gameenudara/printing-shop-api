package lk.oracene.hardware_management_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.oracene.hardware_management_api.dto.request.ResetPasswordRequest;
import lk.oracene.hardware_management_api.dto.request.UpdateUserRequest;
import lk.oracene.hardware_management_api.dto.request.UserRequest;
import lk.oracene.hardware_management_api.dto.response.UserResponse;
import lk.oracene.hardware_management_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create a new user")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @PutMapping("/update/{userId}")
    @Operation(summary = "Update user details")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active users (paginated)")
    public ResponseEntity<Page<UserResponse>> getActiveUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getActiveUsers(
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all users including inactive (paginated)")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getAllUsers(
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @DeleteMapping("/deactivate/{userId}")
    @Operation(summary = "Deactivate a user")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long userId) {
        userService.deactivateUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userId}/activate")
    @Operation(summary = "Reactivate a deactivated user")
    public ResponseEntity<UserResponse> activateUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.activateUser(userId));
    }

    @PatchMapping("/{userId}/reset-password")
    @Operation(summary = "Reset a user's password (ADMIN only)")
    public ResponseEntity<Void> resetPassword(
            @PathVariable Long userId,
            @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(userId, request.getNewPassword());
        return ResponseEntity.noContent().build();
    }
}
