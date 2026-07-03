package lk.oracene.hardware_management_api.repository;

import lk.oracene.hardware_management_api.model.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Page<Supplier> findByIsActiveTrue(Pageable pageable);

    Optional<Supplier> findBySupplierIdAndIsActiveTrue(Long supplierId);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);
}