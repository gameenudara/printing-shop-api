package lk.oracene.hardware_management_api.repository;

import lk.oracene.hardware_management_api.model.SupplierReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierReturnItemRepository extends JpaRepository<SupplierReturnItem, Long> {

    List<SupplierReturnItem> findBySupplierReturn_SupplierReturnId(Long supplierReturnId);
}
