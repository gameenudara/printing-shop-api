package lk.oracene.hardware_management_api.repository;

import lk.oracene.hardware_management_api.model.CustomerReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CustomerReturnItemRepository extends JpaRepository<CustomerReturnItem, Long> {

    List<CustomerReturnItem> findByCustomerReturn_ReturnId(Long returnId);

    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM CustomerReturnItem i WHERE i.saleItem.saleItemId = :saleItemId")
    BigDecimal sumReturnedQuantityBySaleItemId(@Param("saleItemId") Long saleItemId);
}
