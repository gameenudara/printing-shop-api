package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.dto.response.SalesResponse;
import lk.oracene.hardware_management_api.exception.BadRequestException;
import lk.oracene.hardware_management_api.model.PrinterSettings;
import lk.oracene.hardware_management_api.repository.PrinterSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.print.PrintServiceLookup;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrintService {

    private static final byte[] ESC_INIT = {0x1B, 0x40};
    private static final byte[] LF = {0x0A};
    private static final byte[] CUT = {0x1D, 0x56, 0x42, 0x00};
    private static final byte[] KICK_DRAWER = {0x1B, 0x70, 0x00, 0x19, 0x78};

    private final PrinterSettingsRepository printerSettingsRepository;
    private final ReceiptBuilder receiptBuilder;

    public List<String> listPrinters() {
        javax.print.PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        return Arrays.stream(services)
                .map(javax.print.PrintService::getName)
                .toList();
    }

    public void printRaw(String printerName, byte[] data) {
        String sharePath = "\\\\" + "localhost" + "\\" + printerName;
        try (OutputStream os = new FileOutputStream(sharePath)) {
            os.write(data);
            os.flush();
        } catch (IOException e) {
            throw new BadRequestException(
                    "Print failed. Make sure printer '" + printerName
                            + "' is shared in Windows. Error: " + e.getMessage());
        }
    }

    public void printReceipt(SalesResponse sale) {
        PrinterSettings settings = getSettings();
        byte[] receipt = receiptBuilder.buildSalesReceipt(sale, settings);
        printRaw(settings.getPrinterName(), receipt);
        printRaw(settings.getPrinterName(), KICK_DRAWER);
    }

    public void openCashDrawer() {
        printRaw(getConfiguredPrinterName(), KICK_DRAWER);
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
            out.write("        ORACENE HARDWARE        \n".getBytes());
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
