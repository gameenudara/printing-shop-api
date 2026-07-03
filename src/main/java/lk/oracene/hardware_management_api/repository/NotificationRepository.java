package lk.oracene.hardware_management_api.repository;

import lk.oracene.hardware_management_api.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findAll(Pageable pageable);

    long countByIsReadFalse();

    boolean existsByReferenceIdAndCreatedAtBetween(Long referenceId, LocalDateTime from, LocalDateTime to);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.isRead = false")
    void markAllAsRead();

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.notificationId = :id")
    void markAsReadById(@Param("id") Long id);
}
