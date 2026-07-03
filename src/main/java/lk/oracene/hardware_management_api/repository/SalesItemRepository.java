package lk.oracene.hardware_management_api.repository;

import lk.oracene.hardware_management_api.model.SalesItem;
import lk.oracene.hardware_management_api.model.SalesStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SalesItemRepository extends JpaRepository<SalesItem, Long> {

    List<SalesItem> findBySale_SalesId(Long salesId);

    @Modifying
    void deleteBySale_SalesId(Long salesId);

    @Query("SELECT si FROM SalesItem si JOIN FETCH si.product WHERE si.sale.salesId IN :saleIds")
    List<SalesItem> findBySale_SalesIdIn(@Param("saleIds") List<Long> saleIds);

    @Query("SELECT si FROM SalesItem si JOIN FETCH si.product p JOIN FETCH p.category WHERE si.sale.saleDate BETWEEN :from AND :to AND si.sale.status IN :statuses")
    List<SalesItem> findBySaleDateRangeAndStatuses(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("statuses") List<SalesStatus> statuses);

    @Query(value = "SELECT si.product.productId, si.product.name, SUM(si.quantity), SUM(si.lineTotal) FROM SalesItem si WHERE si.sale.saleDate BETWEEN :from AND :to AND si.sale.status IN :statuses GROUP BY si.product.productId, si.product.name ORDER BY SUM(si.quantity) DESC",
           countQuery = "SELECT COUNT(DISTINCT si.product.productId) FROM SalesItem si WHERE si.sale.saleDate BETWEEN :from AND :to AND si.sale.status IN :statuses")
    Page<Object[]> findMostSellingProducts(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("statuses") List<SalesStatus> statuses, Pageable pageable);

    @Query("SELECT p.category.name, COALESCE(SUM(si.lineTotal), 0) FROM SalesItem si JOIN si.product p WHERE si.sale.status NOT IN :excludedStatuses GROUP BY p.category.name ORDER BY SUM(si.lineTotal) DESC")
    List<Object[]> findSalesByCategoryExcludingStatuses(@Param("excludedStatuses") List<SalesStatus> excludedStatuses);
}
