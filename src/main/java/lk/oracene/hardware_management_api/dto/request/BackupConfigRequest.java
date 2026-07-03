package lk.oracene.hardware_management_api.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackupConfigRequest {

    private String saveLocation;
    private Boolean autoBackupEnabled;
    private Integer backupIntervalHours;
}
