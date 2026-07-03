package lk.oracene.hardware_management_api.repository;

import lk.oracene.hardware_management_api.model.SupplierPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierPaymentRepository extends JpaRepository<SupplierPayment, Long> {

    List<SupplierPayment> findBySupplierBill_SupplierBillId(Long supplierBillId);

    Page<SupplierPayment> findBySupplierBill_SupplierBillId(Long supplierBillId, Pageable pageable);
}
