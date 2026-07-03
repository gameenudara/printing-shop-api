package lk.oracene.hardware_management_api.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lk.oracene.hardware_management_api.dto.request.BackupConfigRequest;
import lk.oracene.hardware_management_api.dto.response.BackupConfigResponse;
import lk.oracene.hardware_management_api.dto.response.BackupRecordResponse;
import lk.oracene.hardware_management_api.service.BackupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/backup")
//@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Backup", description = "Backup management APIs")
@RequiredArgsConstructor
public class BackupController {

    private final BackupService backupService;

    // Save or update backup config (location, schedule)
    @PostMapping("/config")
    public ResponseEntity<BackupConfigResponse> saveConfig(
            @RequestBody BackupConfigRequest requestDTO) {
        return ResponseEntity.ok(backupService.saveConfig(requestDTO));
    }

    // Get current saved config
    @GetMapping("/config")
    public ResponseEntity<BackupConfigResponse> getConfig() {
        return ResponseEntity.ok(backupService.getConfig());
    }

    // Manually trigger a backup
    @PostMapping("/trigger")
    public ResponseEntity<BackupRecordResponse> triggerManualBackup() {
        return ResponseEntity.ok(backupService.triggerManualBackup());
    }

    // Get backup history
    @GetMapping("/history")
    public ResponseEntity<List<BackupRecordResponse>> getBackupHistory() {
        return ResponseEntity.ok(backupService.getBackupHistory());
    }
}