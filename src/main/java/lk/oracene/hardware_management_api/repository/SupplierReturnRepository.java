package lk.oracene.hardware_management_api.repository;

import lk.oracene.hardware_management_api.model.SupplierReturn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface SupplierReturnRepository extends JpaRepository<SupplierReturn, Long> {

    Page<SupplierReturn> findAll(Pageable pageable);

    Page<SupplierReturn> findByReturnDateBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);
}
