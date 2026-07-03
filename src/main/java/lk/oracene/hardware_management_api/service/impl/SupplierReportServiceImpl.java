package lk.oracene.hardware_management_api.service.impl;

import lk.oracene.hardware_management_api.dto.response.SupplierOutstandingReportResponse;
import lk.oracene.hardware_management_api.dto.response.SupplierOutstandingReportResponse.SupplierOutstandingRow;
import lk.oracene.hardware_management_api.model.Supplier;
import lk.oracene.hardware_management_api.repository.SupplierBillRepository;
import lk.oracene.hardware_management_api.service.SupplierReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierReportServiceImpl implements SupplierReportService {

    private final SupplierBillRepository supplierBillRepository;

    @Override
    public SupplierOutstandingReportResponse getOutstandingReport() {
        List<Supplier> suppliers = supplierBillRepository.findDistinctSuppliersWithOutstandingBills();

        List<SupplierOutstandingRow> rows = suppliers.stream()
                .map(supplier -> {
                    BigDecimal totalBill = supplierBillRepository.sumTotalAmountBySupplierId(supplier.getSupplierId());
                    BigDecimal paid = supplierBillRepository.sumPaidAmountBySupplierId(supplier.getSupplierId());
                    BigDecimal returned = supplierBillRepository.sumReturnAmountBySupplierId(supplier.getSupplierId());
                    BigDecimal outstanding = totalBill.subtract(paid).subtract(returned);

                    LocalDate oldestDate = supplierBillRepository.findOldestOutstandingBillDateBySupplierId(supplier.getSupplierId());
                    long daysPending = oldestDate != null
                            ? ChronoUnit.DAYS.between(oldestDate, LocalDate.now())
                            : 0;

                    return SupplierOutstandingRow.builder()
                            .supplierId(supplier.getSupplierId())
                            .supplierName(supplier.getName())
                            .phone(supplier.getPhone())
                            .totalBillAmount(totalBill)
                            .paidAmount(paid)
                            .totalReturnAmount(returned)
                            .outstandingAmount(outstanding)
                            .oldestOutstandingBillDate(oldestDate)
                            .daysPending(daysPending)
                            .build();
                })
                .filter(row -> row.getOutstandingAmount().compareTo(BigDecimal.ZERO) > 0)
                .sorted((a, b) -> b.getOutstandingAmount().compareTo(a.getOutstandingAmount()))
                .toList();

        BigDecimal totalOutstanding = rows.stream()
                .map(SupplierOutstandingRow::getOutstandingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return SupplierOutstandingReportResponse.builder()
                .totalSuppliers(rows.size())
                .totalOutstanding(totalOutstanding)
                .suppliers(rows)
                .build();
    }
}
