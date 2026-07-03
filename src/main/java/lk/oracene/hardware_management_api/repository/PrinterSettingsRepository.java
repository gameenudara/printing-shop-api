package lk.oracene.hardware_management_api.repository;

import lk.oracene.hardware_management_api.model.PrinterSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrinterSettingsRepository extends JpaRepository<PrinterSettings, Long> {
    Optional<PrinterSettings> findTopByOrderByIdAsc();
}
