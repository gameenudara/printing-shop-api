package lk.oracene.hardware_management_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.oracene.hardware_management_api.dto.response.BackupResponse;
import lk.oracene.hardware_management_api.service.BackupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/backup")
@RequiredArgsConstructor
@Tag(name = "System", description = "System maintenance APIs")
public class SystemController {

    private final BackupService backupService;

    @PostMapping("/download")
    @Operation(summary = "Trigger an on-demand database backup (ADMIN only)")
    public ResponseEntity<BackupResponse> backup() {
        return ResponseEntity.ok(backupService.runBackup());
    }
}
