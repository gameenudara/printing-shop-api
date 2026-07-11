package lk.oracene.hardware_management_api.repository;

import lk.oracene.hardware_management_api.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByIsActiveTrue(Pageable pageable);

    Optional<Product> findByProductIdAndIsActiveTrue(Long productId);

    @Query("""
            SELECT COUNT(p) > 0 FROM Product p
            WHERE LOWER(p.name) = LOWER(:name)
              AND ((:brand IS NULL AND p.brand IS NULL) OR (:brand IS NOT NULL AND LOWER(p.brand) = LOWER(:brand)))
              AND ((:size IS NULL AND p.size IS NULL) OR (:size IS NOT NULL AND LOWER(p.size) = LOWER(:size)))
              AND ((:colour IS NULL AND p.colour IS NULL) OR (:colour IS NOT NULL AND LOWER(p.colour) = LOWER(:colour)))
            """)
    boolean existsByNameBrandSizeColour(@Param("name") String name, @Param("brand") String brand,
                                        @Param("size") String size, @Param("colour") String colour);

    @Query("""
            SELECT COUNT(p) > 0 FROM Product p
            WHERE LOWER(p.name) = LOWER(:name)
              AND ((:brand IS NULL AND p.brand IS NULL) OR (:brand IS NOT NULL AND LOWER(p.brand) = LOWER(:brand)))
              AND ((:size IS NULL AND p.size IS NULL) OR (:size IS NOT NULL AND LOWER(p.size) = LOWER(:size)))
              AND ((:colour IS NULL AND p.colour IS NULL) OR (:colour IS NOT NULL AND LOWER(p.colour) = LOWER(:colour)))
              AND p.productId <> :productId
            """)
    boolean existsByNameBrandSizeColourExcludingId(@Param("name") String name, @Param("brand") String brand,
                                                   @Param("size") String size, @Param("colour") String colour,
                                                   @Param("productId") Long productId);

    Page<Product> findByCategory_CategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);

    boolean existsByCategory_CategoryIdAndIsActiveTrue(Long categoryId);

    long countByCategory_CategoryIdAndIsActiveTrue(Long categoryId);

    @Query("""
            SELECT p FROM Product p
            WHERE p.isActive = true
              AND LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    Page<Product> searchActive(@Param("q") String query, Pageable pageable);
}
