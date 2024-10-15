package com.finsightanalytics.notificationservice.repository;

import com.finsightanalytics.common.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(Long userId);
    List<Notification> findByUserIdAndType(Long userId, String type);
    List<Notification> findByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end);
    List<Notification> findByDelivered(boolean delivered);
    
    @Query("SELECT n FROM Notification n WHERE n.scheduledTime <= :now AND n.delivered = false")
    List<Notification> findByScheduledTimeBeforeAndDeliveredFalse(@Param("now") LocalDateTime now);
}
