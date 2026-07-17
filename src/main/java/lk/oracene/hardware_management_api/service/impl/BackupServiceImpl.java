package lk.oracene.hardware_management_api.service.impl;

import lk.oracene.hardware_management_api.dto.response.BackupResponse;
import lk.oracene.hardware_management_api.exception.BadRequestException;
import lk.oracene.hardware_management_api.service.BackupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Runs the existing scripts/backup/backup-db.ps1 on the host and reports the result.
 * Only works when this application process itself runs on the Windows host that has
 * Docker Desktop + the mysql-server container - not from inside a containerized deployment
 * of this API.
 */
@Slf4j
@Service
public class BackupServiceImpl implements BackupService {

    private static final Pattern SUCCESS_PATTERN = Pattern.compile("Backup succeeded: (.+)");

    @Value("${backup.script.path}")
    private String scriptPath;

    @Value("${backup.timeout-seconds:300}")
    private long timeoutSeconds;

    @Override
    public BackupResponse runBackup() {
        File script = new File(scriptPath);
        if (!script.exists()) {
            throw new BadRequestException("Backup script not found: " + script.getAbsolutePath());
        }

        ProcessBuilder processBuilder = new ProcessBuilder(
                "powershell", "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", script.getAbsolutePath());
        processBuilder.redirectErrorStream(true);

        StringBuilder output = new StringBuilder();
        Process process;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            log.error("Failed to start backup script", e);
            throw new BadRequestException("Failed to start backup script: " + e.getMessage());
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            log.error("Failed to read backup script output", e);
            throw new BadRequestException("Failed to read backup script output: " + e.getMessage());
        }

        boolean finished;
        try {
            finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BadRequestException("Backup was interrupted");
        }

        if (!finished) {
            process.destroyForcibly();
            log.error("Backup script timed out after {}s. Output:\n{}", timeoutSeconds, output);
            throw new BadRequestException("Backup timed out after " + timeoutSeconds + " seconds");
        }

        if (process.exitValue() != 0) {
            log.error("Backup script failed (exit {}). Output:\n{}", process.exitValue(), output);
            throw new BadRequestException("Backup failed - check server logs for details");
        }

        log.info("Backup script output:\n{}", output);

        String fileName = null;
        Long sizeBytes = null;
        Matcher matcher = SUCCESS_PATTERN.matcher(output);
        if (matcher.find()) {
            Path zipPath = Path.of(matcher.group(1).trim());
            fileName = zipPath.getFileName().toString();
            File zipFile = zipPath.toFile();
            if (zipFile.exists()) {
                sizeBytes = zipFile.length();
            }
        }

        return new BackupResponse("Backup completed successfully", fileName, sizeBytes, LocalDateTime.now());
    }
}
