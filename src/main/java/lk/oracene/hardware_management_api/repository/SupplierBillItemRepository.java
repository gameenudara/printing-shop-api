package lk.oracene.hardware_management_api.repository;

import lk.oracene.hardware_management_api.model.SupplierBillItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierBillItemRepository extends JpaRepository<SupplierBillItem, Long> {

    List<SupplierBillItem> findBySupplierBill_SupplierBillId(Long supplierBillId);

    Page<SupplierBillItem> findBySupplierBill_SupplierBillId(Long supplierBillId, Pageable pageable);

    Page<SupplierBillItem> findByProduct_ProductId(Long productId, Pageable pageable);
}
