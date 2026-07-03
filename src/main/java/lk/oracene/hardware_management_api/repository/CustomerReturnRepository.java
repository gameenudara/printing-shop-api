package lk.oracene.hardware_management_api.repository;

import lk.oracene.hardware_management_api.model.CustomerReturn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CustomerReturnRepository extends JpaRepository<CustomerReturn, Long> {

    List<CustomerReturn> findBySale_SalesId(Long salesId);

    Page<CustomerReturn> findAll(Pageable pageable);

    Page<CustomerReturn> findByReturnDateBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);
}
