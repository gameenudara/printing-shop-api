package lk.oracene.hardware_management_api.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "backup_record")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BackupRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "backup_type", nullable = false)
    private BackupType backupType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BackupStatus backupStatus;

    @Column(name = "file_size_kb")
    private Double fileSizeKb;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}