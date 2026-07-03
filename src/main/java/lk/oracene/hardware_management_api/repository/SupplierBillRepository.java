package lk.oracene.hardware_management_api.repository;

import lk.oracene.hardware_management_api.model.Supplier;
import lk.oracene.hardware_management_api.model.SupplierBill;
import lk.oracene.hardware_management_api.model.SupplierBillStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SupplierBillRepository extends JpaRepository<SupplierBill, Long> {

    Page<SupplierBill> findBySupplier_SupplierId(Long supplierId, Pageable pageable);

    List<SupplierBill> findBySupplier_SupplierIdAndStatus(Long supplierId, SupplierBillStatus status);

    Page<SupplierBill> findBySupplier_SupplierIdAndStatus(Long supplierId, SupplierBillStatus status, Pageable pageable);

    Page<SupplierBill> findBySupplier_SupplierIdAndStatusIn(Long supplierId, List<SupplierBillStatus> statuses, Pageable pageable);

    boolean existsByBillNumber(String billNumber);

    boolean existsBySupplier_SupplierIdAndStatusIn(Long supplierId, List<SupplierBillStatus> statuses);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM SupplierBill b WHERE b.supplier.supplierId = :supplierId AND b.status <> lk.oracene.hardware_management_api.model.SupplierBillStatus.CANCELLED")
    BigDecimal sumTotalAmountBySupplierId(@Param("supplierId") Long supplierId);

    @Query("SELECT COALESCE(SUM(b.paidAmount), 0) FROM SupplierBill b WHERE b.supplier.supplierId = :supplierId AND b.status <> lk.oracene.hardware_management_api.model.SupplierBillStatus.CANCELLED")
    BigDecimal sumPaidAmountBySupplierId(@Param("supplierId") Long supplierId);

    @Query("SELECT COALESCE(SUM(b.totalReturnAmount), 0) FROM SupplierBill b WHERE b.supplier.supplierId = :supplierId AND b.status <> lk.oracene.hardware_management_api.model.SupplierBillStatus.CANCELLED")
    BigDecimal sumReturnAmountBySupplierId(@Param("supplierId") Long supplierId);

    @Query("SELECT DISTINCT b.supplier FROM SupplierBill b WHERE b.status IN (lk.oracene.hardware_management_api.model.SupplierBillStatus.UNPAID, lk.oracene.hardware_management_api.model.SupplierBillStatus.PARTIALLY_PAID, lk.oracene.hardware_management_api.model.SupplierBillStatus.PARTIAL_REFUND)")
    List<Supplier> findDistinctSuppliersWithOutstandingBills();

    @Query("SELECT MIN(b.billDate) FROM SupplierBill b WHERE b.supplier.supplierId = :supplierId AND b.status IN (lk.oracene.hardware_management_api.model.SupplierBillStatus.UNPAID, lk.oracene.hardware_management_api.model.SupplierBillStatus.PARTIALLY_PAID, lk.oracene.hardware_management_api.model.SupplierBillStatus.PARTIAL_REFUND)")
    LocalDate findOldestOutstandingBillDateBySupplierId(@Param("supplierId") Long supplierId);
}
