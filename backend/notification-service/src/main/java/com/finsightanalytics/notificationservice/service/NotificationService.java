    package com.finsightanalytics.notificationservice.service;

    import com.finsightanalytics.common.exception.NotificationNotFoundException;
    import com.finsightanalytics.common.model.Notification;
    import com.finsightanalytics.notificationservice.repository.NotificationRepository;
    import com.finsightanalytics.common.util.EmailUtil;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.kafka.annotation.KafkaListener;
    import org.springframework.scheduling.annotation.Scheduled;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;
    import com.fasterxml.jackson.databind.ObjectMapper;

    import java.time.LocalDateTime;
    import java.util.List;
    import java.util.concurrent.Executors;
    import java.util.concurrent.ScheduledExecutorService;
    import java.util.concurrent.TimeUnit;

    @Service
    public class NotificationService {

        @Autowired
        private NotificationRepository notificationRepository;

        @Autowired
        private EmailUtil emailUtil;

        private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);

        // Retry mechanism configuration
        private static final int MAX_RETRY_ATTEMPTS = 3;
        private static final long RETRY_DELAY_SECONDS = 10;

        @Transactional
        public Notification createNotification(Notification notification) {
            // Save the notification to the database
            notification.setTimestamp(LocalDateTime.now());
            notification = notificationRepository.save(notification);

            // If no scheduled time is provided, send immediately
            if (notification.getScheduledTime() == null) {
                sendNotification(notification);
            }

            return notification;
        }

        @Scheduled(fixedRate = 60000) // Check every minute
        public void sendScheduledNotifications() {
            List<Notification> notifications = notificationRepository.findByScheduledTimeBeforeAndDeliveredFalse(LocalDateTime.now());
            for (Notification notification : notifications) {
                sendNotification(notification);
            }
        }

        private void sendNotification(Notification notification) {
            // Send the notification based on its type
            switch (notification.getType()) {
                case "email":
                    sendEmail(notification);
                    break;
                case "sms":
                    sendSms(notification);
                    break;
                case "in-app":
                    sendInAppNotification(notification);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown notification type: " + notification.getType());
            }

            // Mark the notification as delivered
            notification.setDelivered(true);
            notificationRepository.save(notification);
        }

        // Method to send email notification
        private void sendEmail(Notification notification) {
            String subject = "Notification: " + notification.getType();
            emailUtil.sendSimpleMessage(notification.getUserEmail(), subject, notification.getMessage());
        }

        // Method to send SMS notification
        private void sendSms(Notification notification) {
            //ToDo: Implement SMS sending logic here
            System.out.println("Sending SMS to " + notification.getUserPhone() + ": " + notification.getMessage());
        }

        // Method to send in-app notification
        private void sendInAppNotification(Notification notification) {
            //ToDo: Implement in-app notification sending logic here
            System.out.println("Sending in-app notification to user " + notification.getUserId() + ": " + notification.getMessage());
        }

        @KafkaListener(topics = "notification-topic", groupId = "notification-group")
        public void listen(String message) {
            // Parse the message to create Notification object
            Notification notification = parseMessage(message);
            retrySendNotification(notification, 0);
        }

        // Retry logic
        private void retrySendNotification(Notification notification, int attempt) {
            try {
                createNotification(notification);
            } catch (Exception e) {
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    executorService.schedule(() -> retrySendNotification(notification, attempt + 1), RETRY_DELAY_SECONDS, TimeUnit.SECONDS);
                } else {
                    // Handle max retry attempts reached
                    System.err.println("Max retry attempts reached for notification: " + notification.getId());
                    // Optionally, send an alert or log the error to a monitoring system
                }
            }
        }

        private Notification parseMessage(String message) {
            ObjectMapper objectMapper = new ObjectMapper();
            Notification notification;
            try {
                notification = objectMapper.readValue(message, Notification.class);
            } catch (Exception e) {
                // Throw a custom exception when the message is invalid or cannot be parsed
                throw new NotificationNotFoundException("Failed to parse notification message: " + message, e);
            }
            return notification;
        }

        public Notification getNotificationById(Long id) {
            return notificationRepository.findById(id).orElseThrow(() -> new NotificationNotFoundException("Notification not found with id: " + id));
        }

        public List<Notification> getAllNotifications() {
            return notificationRepository.findAll();
        }

        public List<Notification> getNotificationsByUserId(Long userId) {
            return notificationRepository.findByUserId(userId);
        }

        public List<Notification> getNotificationsByUserIdAndType(Long userId, String type) {
            return notificationRepository.findByUserIdAndType(userId, type);
        }

        public List<Notification> getNotificationsByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end) {
            return notificationRepository.findByUserIdAndTimestampBetween(userId, start, end);
        }

        public List<Notification> getNotificationsByDelivered(boolean delivered) {
            return notificationRepository.findByDelivered(delivered);
        }

        public Notification updateNotification(Notification notification) {
            if (!notificationRepository.existsById(notification.getId())) {
                throw new NotificationNotFoundException("Notification not found with id: " + notification.getId());
            }
            return notificationRepository.save(notification);
        }

        public void deleteNotification(Long id) {
            if (!notificationRepository.existsById(id)) {
                throw new NotificationNotFoundException("Notification not found with id: " + id);
            }
            notificationRepository.deleteById(id);
        }
    }
