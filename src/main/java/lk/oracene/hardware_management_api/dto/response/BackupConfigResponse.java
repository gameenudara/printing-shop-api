package lk.oracene.hardware_management_api.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackupConfigResponse {
    private Long id;
    private String saveLocation;
    private Boolean autoBackupEnabled;
    private Integer backupIntervalHours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
