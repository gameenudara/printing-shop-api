package lk.oracene.hardware_management_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.oracene.hardware_management_api.dto.response.SalesResponse;
import lk.oracene.hardware_management_api.service.PrintService;
import lk.oracene.hardware_management_api.service.SalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/print")
@Tag(name = "Print", description = "Thermal printer APIs")
@RequiredArgsConstructor
public class PrintController {

    private final PrintService printService;
    private final SalesService salesService;

    @GetMapping("/list")
    @Operation(summary = "List all OS printers")
    public ResponseEntity<List<String>> listPrinters() {
        return ResponseEntity.ok(printService.listPrinters());
    }

    @GetMapping("/receipt/{salesId}")
    @Operation(summary = "Print receipt for a specific sale")
    public ResponseEntity<Map<String, String>> printReceipt(@PathVariable Long salesId) {
        SalesResponse sale = salesService.getSaleById(salesId);
        printService.printReceipt(sale);
        return ResponseEntity.ok(Map.of("message", "Receipt sent to printer for " + sale.getInvoiceNumber()));
    }

    @PostMapping("/open-drawer")
    @Operation(summary = "Open cash drawer")
    public ResponseEntity<Map<String, String>> openDrawer() {
        printService.openCashDrawer();
        return ResponseEntity.ok(Map.of("message", "Cash drawer opened"));
    }

    @PostMapping("/test")
    @Operation(summary = "Print a test receipt")
    public ResponseEntity<Map<String, String>> printTest() {
        printService.printTestReceipt();
        return ResponseEntity.ok(Map.of("message", "Test receipt sent to printer"));
    }
}
