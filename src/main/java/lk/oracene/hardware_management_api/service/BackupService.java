package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.dto.request.BackupConfigRequest;
import lk.oracene.hardware_management_api.dto.response.BackupConfigResponse;
import lk.oracene.hardware_management_api.dto.response.BackupRecordResponse;
import java.util.List;

public interface BackupService {
    BackupConfigResponse saveConfig(BackupConfigRequest requestDTO);
    BackupConfigResponse getConfig();
    BackupRecordResponse triggerManualBackup();
    List<BackupRecordResponse> getBackupHistory();
}