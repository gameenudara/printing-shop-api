package lk.oracene.hardware_management_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.oracene.hardware_management_api.dto.request.PrinterSettingsRequest;
import lk.oracene.hardware_management_api.dto.response.PrinterSettingsResponse;
import lk.oracene.hardware_management_api.service.PrinterSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/printer/settings")
@Tag(name = "Print", description = "Thermal printer APIs")
@RequiredArgsConstructor
public class PrinterSettingsController {

    private final PrinterSettingsService printerSettingsService;

    @GetMapping
    @Operation(summary = "Get printer settings")
    public ResponseEntity<PrinterSettingsResponse> getSettings() {
        return ResponseEntity.ok(printerSettingsService.getSettings());
    }

    @PutMapping
    @Operation(summary = "Update printer settings")
    public ResponseEntity<PrinterSettingsResponse> updateSettings(
            @RequestBody PrinterSettingsRequest request) {
        return ResponseEntity.ok(printerSettingsService.updateSettings(request));
    }
}
