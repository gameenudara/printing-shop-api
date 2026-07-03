package lk.oracene.hardware_management_api.service.impl;

import lk.oracene.hardware_management_api.dto.request.BackupConfigRequest;
import lk.oracene.hardware_management_api.dto.response.BackupConfigResponse;
import lk.oracene.hardware_management_api.dto.response.BackupRecordResponse;
import lk.oracene.hardware_management_api.model.BackupStatus;
import lk.oracene.hardware_management_api.model.BackupType;
import lk.oracene.hardware_management_api.model.BackupConfig;
import lk.oracene.hardware_management_api.model.BackupRecord;
import lk.oracene.hardware_management_api.repository.BackupConfigRepository;
import lk.oracene.hardware_management_api.repository.BackupRecordRepository;
import lk.oracene.hardware_management_api.service.BackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackupServiceImpl implements BackupService {

    private final BackupConfigRepository backupConfigRepository;
    private final BackupRecordRepository backupRecordRepository;
    private final DataSource dataSource;

    @Value("${backup.database.name}")
    private String databaseName;

    // ─── Save or Update Config ───────────────────────────────────────────────

    @Override
    public BackupConfigResponse saveConfig(BackupConfigRequest requestDTO) {
        BackupConfig config = backupConfigRepository.findTopByOrderByIdAsc()
                .orElse(BackupConfig.builder().build());

        config.setSaveLocation(requestDTO.getSaveLocation());
        config.setAutoBackupEnabled(requestDTO.getAutoBackupEnabled());
        config.setBackupIntervalHours(requestDTO.getBackupIntervalHours());

        BackupConfig saved = backupConfigRepository.save(config);
        return mapToConfigResponse(saved);
    }

    // ─── Get Saved Config ────────────────────────────────────────────────────

    @Override
    public BackupConfigResponse getConfig() {
        BackupConfig config = backupConfigRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new RuntimeException("Backup configuration not found. Please set it up first."));
        return mapToConfigResponse(config);
    }

    // ─── Manual Backup ───────────────────────────────────────────────────────

    @Override
    public BackupRecordResponse triggerManualBackup() {
        return performBackup(BackupType.MANUAL);
    }

    // ─── Auto Backup Scheduler (cron from application.properties) ────────────

    @Scheduled(cron = "${backup.scheduler.cron}")
    public void autoBackupScheduler() {
        backupConfigRepository.findTopByOrderByIdAsc().ifPresent(config -> {
            if (Boolean.TRUE.equals(config.getAutoBackupEnabled())) {
                log.info("Auto backup triggered at {}", LocalDateTime.now());
                performBackup(BackupType.AUTO);
            }
        });
    }

    // ─── Backup History ──────────────────────────────────────────────────────

    @Override
    public List<BackupRecordResponse> getBackupHistory() {
        return backupRecordRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToRecordResponse)
                .collect(Collectors.toList());
    }

    // ─── Core Backup Logic (JDBC-based, no external tools required) ────────

    private BackupRecordResponse performBackup(BackupType backupType) {
        BackupConfig config = backupConfigRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new RuntimeException("Backup configuration not set. Please configure the save location first."));

        String saveLocation = config.getSaveLocation();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = databaseName + "_backup_" + timestamp + ".sql";
        String fullPath = saveLocation + File.separator + fileName;

        File directory = new File(saveLocation);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        BackupRecord record = BackupRecord.builder()
                .fileName(fileName)
                .filePath(fullPath)
                .backupType(backupType)
                .backupStatus(BackupStatus.SUCCESS)
                .build();

        try (Connection connection = dataSource.getConnection();
             BufferedWriter writer = new BufferedWriter(new FileWriter(fullPath))) {

            writer.write("-- JDBC Backup of database: " + databaseName);
            writer.newLine();
            writer.write("-- Date: " + LocalDateTime.now());
            writer.newLine();
            writer.write("-- -----------------------------------------------");
            writer.newLine();
            writer.newLine();
            writer.write("SET FOREIGN_KEY_CHECKS=0;");
            writer.newLine();
            writer.write("SET SQL_MODE='NO_AUTO_VALUE_ON_ZERO';");
            writer.newLine();
            writer.newLine();

            List<String> tables = new ArrayList<>();
            try (ResultSet rs = connection.getMetaData().getTables(databaseName, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            }

            for (String table : tables) {
                writer.write("-- -----------------------------------------------");
                writer.newLine();
                writer.write("-- Table structure for `" + table + "`");
                writer.newLine();
                writer.write("-- -----------------------------------------------");
                writer.newLine();
                writer.write("DROP TABLE IF EXISTS `" + table + "`;");
                writer.newLine();

                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE `" + table + "`")) {
                    if (rs.next()) {
                        writer.write(rs.getString(2) + ";");
                        writer.newLine();
                        writer.newLine();
                    }
                }

                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM `" + table + "`")) {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    boolean hasData = false;

                    while (rs.next()) {
                        if (!hasData) {
                            writer.write("-- Data for `" + table + "`");
                            writer.newLine();
                            hasData = true;
                        }
                        StringBuilder sb = new StringBuilder();
                        sb.append("INSERT INTO `").append(table).append("` VALUES (");
                        for (int i = 1; i <= columnCount; i++) {
                            if (i > 1) sb.append(", ");
                            Object value = rs.getObject(i);
                            if (value == null) {
                                sb.append("NULL");
                            } else if (value instanceof Number) {
                                sb.append(value);
                            } else if (value instanceof Boolean) {
                                sb.append((Boolean) value ? 1 : 0);
                            } else if (value instanceof byte[]) {
                                sb.append("X'").append(bytesToHex((byte[]) value)).append("'");
                            } else {
                                sb.append("'").append(escapeSql(value.toString())).append("'");
                            }
                        }
                        sb.append(");");
                        writer.write(sb.toString());
                        writer.newLine();
                    }
                }
                writer.newLine();
            }

            writer.write("SET FOREIGN_KEY_CHECKS=1;");
            writer.newLine();
            writer.flush();

            File backupFile = new File(fullPath);
            record.setFileSizeKb(backupFile.length() / 1024.0);
            log.info("Backup success [{}]: {} ({} KB)", backupType, fileName, record.getFileSizeKb());

        } catch (Exception e) {
            record.setBackupStatus(BackupStatus.FAILED);
            record.setErrorMessage(e.getMessage());
            log.error("Backup exception [{}]: {}", backupType, e.getMessage());
        }

        BackupRecord saved = backupRecordRepository.save(record);
        return mapToRecordResponse(saved);
    }

    private String escapeSql(String value) {
        return value.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\0", "\\0");
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    // ─── Mappers ─────────────────────────────────────────────────────────────

    private BackupConfigResponse mapToConfigResponse(BackupConfig config) {
        return BackupConfigResponse.builder()
                .id(config.getId())
                .saveLocation(config.getSaveLocation())
                .autoBackupEnabled(config.getAutoBackupEnabled())
                .backupIntervalHours(config.getBackupIntervalHours())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .createdBy(config.getCreatedBy())
                .updatedBy(config.getUpdatedBy())
                .build();
    }

    private BackupRecordResponse mapToRecordResponse(BackupRecord record) {
        return BackupRecordResponse.builder()
                .id(record.getId())
                .fileName(record.getFileName())
                .filePath(record.getFilePath())
                .backupType(record.getBackupType())
                .status(record.getBackupStatus())
                .fileSizeKb(record.getFileSizeKb())
                .errorMessage(record.getErrorMessage())
                .createdAt(record.getCreatedAt())
                .createdBy(record.getCreatedBy())
                .build();
    }
}