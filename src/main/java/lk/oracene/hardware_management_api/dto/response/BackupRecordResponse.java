package lk.oracene.hardware_management_api.dto.response;

import lk.oracene.hardware_management_api.model.BackupStatus;
import lk.oracene.hardware_management_api.model.BackupType;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackupRecordResponse {
    private Long id;
    private String fileName;
    private String filePath;
    private BackupType backupType;
    private BackupStatus status;
    private Double fileSizeKb;
    private String errorMessage;
    private LocalDateTime createdAt;
    private String createdBy;
}
