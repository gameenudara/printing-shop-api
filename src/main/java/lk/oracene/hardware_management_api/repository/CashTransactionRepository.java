package lk.oracene.hardware_management_api.repository;

import lk.oracene.hardware_management_api.model.CashTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CashTransactionRepository extends JpaRepository<CashTransaction, Long> {

    List<CashTransaction> findBySession_SessionIdOrderByCreatedAtAsc(Long sessionId);

    Page<CashTransaction> findBySession_SessionIdOrderByCreatedAtDesc(Long sessionId, Pageable pageable);
}
