package lk.oracene.hardware_management_api.dto.response;

import lk.oracene.hardware_management_api.model.RoleType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {

    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private RoleType role;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
