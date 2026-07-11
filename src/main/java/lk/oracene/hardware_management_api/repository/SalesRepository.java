package lk.oracene.hardware_management_api.repository;

import lk.oracene.hardware_management_api.model.Customer;
import lk.oracene.hardware_management_api.model.Sales;
import lk.oracene.hardware_management_api.model.SalesStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesRepository extends JpaRepository<Sales, Long> {

    Optional<Sales> findByInvoiceNumber(String invoiceNumber);

    Page<Sales> findByInvoiceNumberContainingIgnoreCase(String invoiceNumber, Pageable pageable);

    Page<Sales> findByCustomer_CustomerId(Long customerId, Pageable pageable);

    Page<Sales> findByCustomer_CustomerIdAndStatus(Long customerId, SalesStatus status, Pageable pageable);

    Page<Sales> findByCustomer_CustomerIdAndStatusIn(Long customerId, List<SalesStatus> statuses, Pageable pageable);

    boolean existsByCustomer_CustomerIdAndStatus(Long customerId, SalesStatus status);

    List<Sales> findByStatus(SalesStatus status);

    Page<Sales> findByStatus(SalesStatus status, Pageable pageable);

    Page<Sales> findByStatusNot(SalesStatus status, Pageable pageable);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sales s WHERE s.customer.customerId = :customerId AND s.status <> lk.oracene.hardware_management_api.model.SalesStatus.CANCELLED")
    BigDecimal sumTotalAmountByCustomerId(@Param("customerId") Long customerId);

    List<Sales> findBySaleDateBetweenOrderBySaleDateAsc(LocalDateTime from, LocalDateTime to);

    @Query("SELECT COUNT(s) FROM Sales s WHERE s.saleDate BETWEEN :from AND :to AND s.status NOT IN :excludedStatuses")
    Long countByDateRangeExcludingStatuses(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("excludedStatuses") List<SalesStatus> excludedStatuses);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sales s WHERE s.saleDate BETWEEN :from AND :to AND s.status NOT IN :excludedStatuses")
    BigDecimal sumRevenueByDateRangeExcludingStatuses(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("excludedStatuses") List<SalesStatus> excludedStatuses);

    @Query("SELECT DISTINCT s.customer FROM Sales s WHERE s.status = lk.oracene.hardware_management_api.model.SalesStatus.UNPAID AND s.customer IS NOT NULL")
    List<Customer> findDistinctCustomersWithPendingSales();

    @Query("SELECT MIN(s.saleDate) FROM Sales s WHERE s.customer.customerId = :customerId AND s.status = lk.oracene.hardware_management_api.model.SalesStatus.UNPAID")
    LocalDateTime findOldestPendingSaleDateByCustomerId(@Param("customerId") Long customerId);
}
