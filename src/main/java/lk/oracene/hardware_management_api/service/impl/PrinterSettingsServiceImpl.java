package lk.oracene.hardware_management_api.service.impl;

import lk.oracene.hardware_management_api.dto.request.PrinterSettingsRequest;
import lk.oracene.hardware_management_api.dto.response.PrinterSettingsResponse;
import lk.oracene.hardware_management_api.model.PrinterSettings;
import lk.oracene.hardware_management_api.repository.PrinterSettingsRepository;
import lk.oracene.hardware_management_api.service.PrinterSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PrinterSettingsServiceImpl implements PrinterSettingsService {

    private final PrinterSettingsRepository printerSettingsRepository;

    @Override
    public PrinterSettingsResponse getSettings() {
        PrinterSettings settings = getOrCreateSettings();
        return mapToResponse(settings);
    }

    @Override
    public PrinterSettingsResponse updateSettings(PrinterSettingsRequest request) {
        PrinterSettings settings = getOrCreateSettings();

        if (request.getPrinterName() != null) {
            settings.setPrinterName(request.getPrinterName());
        }
        if (request.getPaperWidthDots() != null) {
            settings.setPaperWidthDots(request.getPaperWidthDots());
        }
        if (request.getAutoCut() != null) {
            settings.setAutoCut(request.getAutoCut());
        }
        if (request.getCharset() != null) {
            settings.setCharset(request.getCharset());
        }
        if (request.getHeaderText() != null) {
            settings.setHeaderText(request.getHeaderText());
        }

        PrinterSettings saved = printerSettingsRepository.save(settings);
        return mapToResponse(saved);
    }

    private PrinterSettings getOrCreateSettings() {
        return printerSettingsRepository.findTopByOrderByIdAsc()
                .orElseGet(() -> printerSettingsRepository.save(
                        PrinterSettings.builder()
                                .paperWidthDots(576)
                                .autoCut(true)
                                .charset("CP437")
                                .build()
                ));
    }

    private PrinterSettingsResponse mapToResponse(PrinterSettings settings) {
        return PrinterSettingsResponse.builder()
                .id(settings.getId())
                .printerName(settings.getPrinterName())
                .paperWidthDots(settings.getPaperWidthDots())
                .autoCut(settings.getAutoCut())
                .charset(settings.getCharset())
                .headerText(settings.getHeaderText())
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .createdBy(settings.getCreatedBy())
                .updatedBy(settings.getUpdatedBy())
                .build();
    }
}
