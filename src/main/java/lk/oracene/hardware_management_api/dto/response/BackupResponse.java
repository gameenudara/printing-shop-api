package lk.oracene.hardware_management_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BackupResponse {

    private String message;
    private String fileName;
    private Long sizeBytes;
    private LocalDateTime completedAt;
}
