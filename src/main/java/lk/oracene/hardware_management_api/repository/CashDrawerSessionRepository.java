package lk.oracene.hardware_management_api.repository;

import lk.oracene.hardware_management_api.model.CashDrawerSession;
import lk.oracene.hardware_management_api.model.CashDrawerSessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CashDrawerSessionRepository extends JpaRepository<CashDrawerSession, Long> {

    Optional<CashDrawerSession> findFirstByOrderByCreatedAtDesc();

    Page<CashDrawerSession> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Optional<CashDrawerSession> findFirstByStatusOrderByCreatedAtDesc(CashDrawerSessionStatus status);

    List<CashDrawerSession> findByStatus(CashDrawerSessionStatus status);
}
