package lk.oracene.hardware_management_api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_is_read", columnList = "is_read"),
        @Index(name = "idx_notification_ref_created", columnList = "reference_id, created_at")
})
@Data
@EqualsAndHashCode(callSuper = false)
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String message;

    private Long referenceId;

    private Boolean isRead = false;
}
