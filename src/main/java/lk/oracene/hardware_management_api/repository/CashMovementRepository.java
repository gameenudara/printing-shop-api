package lk.oracene.hardware_management_api.repository;

import lk.oracene.hardware_management_api.model.CashMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CashMovementRepository extends JpaRepository<CashMovement, Long> {

    List<CashMovement> findBySession_SessionIdOrderByCreatedAtAsc(Long sessionId);
}
