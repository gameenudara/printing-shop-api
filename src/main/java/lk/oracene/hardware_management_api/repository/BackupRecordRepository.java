package lk.oracene.hardware_management_api.repository;

import lk.oracene.hardware_management_api.model.BackupRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BackupRecordRepository extends JpaRepository<BackupRecord, Long> {
    List<BackupRecord> findAllByOrderByCreatedAtDesc();
}
