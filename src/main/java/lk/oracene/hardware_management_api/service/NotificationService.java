package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.dto.response.NotificationResponse;
import lk.oracene.hardware_management_api.model.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    void createNotification(NotificationType type, String message, Long referenceId);

    Page<NotificationResponse> getAll(Pageable pageable);

    long getUnreadCount();

    void markAsRead(Long notificationId);

    void markAllAsRead();
}
