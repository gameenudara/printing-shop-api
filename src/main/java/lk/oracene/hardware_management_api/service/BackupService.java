package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.dto.response.BackupResponse;

public interface BackupService {

    BackupResponse runBackup();
}
