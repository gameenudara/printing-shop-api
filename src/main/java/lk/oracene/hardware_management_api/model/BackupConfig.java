package lk.oracene.hardware_management_api.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "backup_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BackupConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "save_location", nullable = false)
    private String saveLocation;

    @Column(name = "auto_backup_enabled")
    private Boolean autoBackupEnabled = false;

    @Column(name = "backup_interval_hours")
    private Integer backupIntervalHours = 24;
}