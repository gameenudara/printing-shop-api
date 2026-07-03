package lk.oracene.hardware_management_api.dto.response;

import lk.oracene.hardware_management_api.model.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {

    private Long notificationId;
    private NotificationType type;
    private String message;
    private Long referenceId;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
