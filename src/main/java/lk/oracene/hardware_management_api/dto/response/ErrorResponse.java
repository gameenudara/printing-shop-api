package lk.oracene.hardware_management_api.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ErrorResponse {

    private String message;
    private LocalDateTime timestamp;
    private Integer status;
    private Map<String, String> errors;
}
