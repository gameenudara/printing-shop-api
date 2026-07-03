package lk.oracene.hardware_management_api.repository;

import lk.oracene.hardware_management_api.model.Cheque;
import lk.oracene.hardware_management_api.model.ChequeStatus;
import lk.oracene.hardware_management_api.model.ChequeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChequeRepository extends JpaRepository<Cheque, Long> {

    boolean existsByChequeNumber(String chequeNumber);

    Page<Cheque> findByCustomer_CustomerId(Long customerId, Pageable pageable);

    Page<Cheque> findByCustomer_CustomerIdAndChequeStatus(Long customerId, ChequeStatus chequeStatus, Pageable pageable);

    Page<Cheque> findBySupplier_SupplierId(Long supplierId, Pageable pageable);

    Page<Cheque> findBySupplier_SupplierIdAndChequeStatus(Long supplierId, ChequeStatus chequeStatus, Pageable pageable);

    Page<Cheque> findByChequeType(ChequeType chequeType, Pageable pageable);

    Page<Cheque> findByChequeStatus(ChequeStatus chequeStatus, Pageable pageable);

    List<Cheque> findByChequeStatusAndDueDateLessThanEqual(ChequeStatus chequeStatus, LocalDate date);

    Optional<Cheque> findByChequeNumber(String chequeNumber);
}
