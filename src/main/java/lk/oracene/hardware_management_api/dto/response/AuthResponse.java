package lk.oracene.hardware_management_api.dto.response;

import lk.oracene.hardware_management_api.model.RoleType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String token;
    private String tokenType;
    private String refreshToken;
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private RoleType role;
}
