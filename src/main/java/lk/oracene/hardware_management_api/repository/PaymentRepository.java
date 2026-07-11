package lk.oracene.hardware_management_api.repository;

import lk.oracene.hardware_management_api.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findBySale_SalesId(Long salesId);

    List<Payment> findBySale_SalesIdIn(List<Long> saleIds);

    Page<Payment> findBySale_Customer_CustomerId(Long customerId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.paidAmount), 0) FROM Payment p WHERE p.sale.customer.customerId = :customerId AND p.status = lk.oracene.hardware_management_api.model.PaymentStatus.SUCCESS AND p.sale.status <> lk.oracene.hardware_management_api.model.SalesStatus.CANCELLED")
    BigDecimal sumPaidAmountByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT COALESCE(SUM(p.paidAmount), 0) FROM Payment p WHERE p.paidAt BETWEEN :from AND :to AND p.status = lk.oracene.hardware_management_api.model.PaymentStatus.SUCCESS")
    BigDecimal sumSuccessfulPaymentsBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
