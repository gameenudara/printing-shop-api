package lk.oracene.hardware_management_api.repository;

import lk.oracene.hardware_management_api.model.BackupConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BackupConfigRepository extends JpaRepository<BackupConfig, Long> {
    Optional<BackupConfig> findTopByOrderByIdAsc();
}
