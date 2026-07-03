package lk.oracene.hardware_management_api.service.impl;

import lk.oracene.hardware_management_api.dto.response.NotificationResponse;
import lk.oracene.hardware_management_api.exception.NotFoundException;
import lk.oracene.hardware_management_api.model.Notification;
import lk.oracene.hardware_management_api.model.NotificationType;
import lk.oracene.hardware_management_api.repository.NotificationRepository;
import lk.oracene.hardware_management_api.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public void createNotification(NotificationType type, String message, Long referenceId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        if (notificationRepository.existsByReferenceIdAndCreatedAtBetween(referenceId, startOfDay, endOfDay)) {
            return;
        }

        Notification notification = new Notification();
        notification.setType(type);
        notification.setMessage(message);
        notification.setReferenceId(referenceId);
        notification.setIsRead(false);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getAll(Pageable pageable) {
        return notificationRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        return notificationRepository.countByIsReadFalse();
    }

    @Override
    public void markAsRead(Long notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new NotFoundException("Notification not found with id: " + notificationId);
        }
        notificationRepository.markAsReadById(notificationId);
    }

    @Override
    public void markAllAsRead() {
        notificationRepository.markAllAsRead();
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .type(notification.getType())
                .message(notification.getMessage())
                .referenceId(notification.getReferenceId())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
