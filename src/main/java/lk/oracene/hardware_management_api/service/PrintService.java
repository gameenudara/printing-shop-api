package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.client.PrinterAgentClient;
import lk.oracene.hardware_management_api.dto.response.SalesResponse;
import lk.oracene.hardware_management_api.exception.BadRequestException;
import lk.oracene.hardware_management_api.model.PrinterSettings;
import lk.oracene.hardware_management_api.repository.PrinterSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrintService {

    private static final byte[] ESC_INIT = {0x1B, 0x40};
    private static final byte[] LF = {0x0A};
    private static final byte[] CUT = {0x1D, 0x56, 0x42, 0x00};
    private static final int DEFAULT_DRAWER_PIN = 0;

    private final PrinterSettingsRepository printerSettingsRepository;
    private final ReceiptBuilder receiptBuilder;
    private final PrinterAgentClient printerAgentClient;

    public List<String> listPrinters() {
        return printerAgentClient.listPrinters();
    }

    public void printRaw(String printerName, byte[] data) {
        printerAgentClient.printRaw(printerName, data);
    }

    public void printReceipt(SalesResponse sale) {
        PrinterSettings settings = getSettings();
        byte[] receipt = receiptBuilder.buildSalesReceipt(sale, settings);
        printRaw(settings.getPrinterName(), receipt);
        printerAgentClient.openDrawer(settings.getPrinterName(), DEFAULT_DRAWER_PIN);
    }

    public void openCashDrawer() {
        printerAgentClient.openDrawer(getConfiguredPrinterName(), DEFAULT_DRAWER_PIN);
    }

    public void printTestReceipt() {
        String printerName = getConfiguredPrinterName();
        printRaw(printerName, buildTestReceipt(printerName));
    }

    private PrinterSettings getSettings() {
        PrinterSettings settings = printerSettingsRepository.findTopByOrderByIdAsc()
                .orElse(null);

        if (settings == null || settings.getPrinterName() == null || settings.getPrinterName().isBlank()) {
            throw new BadRequestException(
                    "Printer not configured. Set the printer name in PUT /api/v1/printer/settings first.");
        }

        return settings;
    }

    private String getConfiguredPrinterName() {
        return getSettings().getPrinterName();
    }

    private byte[] buildTestReceipt(String printerName) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(ESC_INIT);
            out.write("================================\n".getBytes());
            out.write("        ORACENE software        \n".getBytes());
            out.write("================================\n".getBytes());
            out.write("  Printer Test Receipt\n".getBytes());
            out.write(("  Printer: " + printerName + "\n").getBytes());
            out.write("  Status:  OK\n".getBytes());
            out.write("================================\n".getBytes());
            for (int i = 0; i < 6; i++) {
                out.write(LF);
            }
            out.write(CUT);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to build receipt", e);
        }
    }
}
