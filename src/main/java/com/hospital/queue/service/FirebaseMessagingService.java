package com.hospital.queue.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import com.google.api.core.ApiFuture;
import com.hospital.queue.dto.PatientNotificationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Service for sending notifications via Firebase Cloud Messaging
 */
@Service
public class FirebaseMessagingService {
    
    private static final Logger logger = LoggerFactory.getLogger(FirebaseMessagingService.class);
    
    @Value("${firebase.credentials.path}")
    private String credentialsPath;
    
    @Value("${firebase.database.url:#{null}}")
    private String databaseUrl;
    
    /**
     * Initialize Firebase with service account credentials
     */
    @PostConstruct
    public void initialize() {
        try {
            logger.info("Initializing Firebase with credentials path: {}", credentialsPath);
            logger.info("Database URL: {}", databaseUrl);
            
            if (FirebaseApp.getApps().isEmpty()) {
                logger.info("No Firebase apps found, initializing new app");
                
                try {
                    ClassPathResource resource = new ClassPathResource(credentialsPath);
                    logger.info("Loading credentials from: {}, exists: {}", 
                            resource.getPath(), resource.exists());
                    
                    GoogleCredentials credentials = GoogleCredentials
                            .fromStream(resource.getInputStream());
                    logger.info("Credentials loaded successfully");
                    
                    FirebaseOptions.Builder builder = FirebaseOptions.builder()
                            .setCredentials(credentials);
                    
                    if (databaseUrl != null && !databaseUrl.isEmpty()) {
                        builder.setDatabaseUrl(databaseUrl);
                        logger.info("Database URL set: {}", databaseUrl);
                    }
                    
                    FirebaseOptions options = builder.build();
                    FirebaseApp app = FirebaseApp.initializeApp(options);
                    
                    logger.info("Firebase application has been initialized: {}", app.getName());
                } catch (IOException e) {
                    logger.error("Error loading Firebase credentials: {}", e.getMessage(), e);
                    throw e;
                }
            } else {
                logger.info("Firebase app already initialized: {}", FirebaseApp.getInstance().getName());
            }
        } catch (Exception e) {
            logger.error("Error initializing Firebase: {}", e.getMessage(), e);
            throw new RuntimeException("Error initializing Firebase", e);
        }
    }
    
    /**
     * Send a notification to a single device
     * 
     * @param notification Notification data
     * @param token Device token
     * @return Message ID
     */
    public String sendNotification(PatientNotificationDto notification, String token) {
        try {
            logger.info("Preparing to send notification to token: {}", token);
            logger.info("Notification content: title={}, body={}", 
                    notification.getTitle(), notification.getBody());
            
            Message message = createMessage(notification, token);
            logger.info("Message created, sending via FCM...");
            
            String response = FirebaseMessaging.getInstance().send(message);
            logger.info("Successfully sent message to token {}: {}", token, response);
            return response;
        } catch (FirebaseMessagingException e) {
            logger.error("Error sending Firebase notification: {}", e.getMessage(), e);
            logger.error("Error details - messaging error code: {}", e.getMessagingErrorCode());
            throw new RuntimeException("Error sending Firebase notification", e);
        } catch (Exception e) {
            logger.error("Unexpected error sending notification: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error sending notification", e);
        }
    }
    
    /**
     * Send a notification to multiple devices
     * 
     * @param notification Notification data
     * @param tokens List of device tokens
     * @return List of message IDs
     */
    public List<String> sendMulticastNotification(PatientNotificationDto notification, List<String> tokens) {
        try {
            if (tokens.isEmpty()) {
                logger.warn("No device tokens provided for multicast notification");
                return Collections.emptyList();
            }
            
            logger.info("Sending notifications to {} devices individually instead of batch", tokens.size());
            List<String> messageIds = new ArrayList<>();
            List<String> failedTokens = new ArrayList<>();
            
            // Send messages individually instead of using multicast
            for (String token : tokens) {
                try {
                    // Create individual message for each token
                    Message message = createMessage(notification, token);
                    String messageId = FirebaseMessaging.getInstance().send(message);
                    messageIds.add(messageId);
                    logger.info("Successfully sent message to token {}: {}", token, messageId);
                } catch (FirebaseMessagingException e) {
                    logger.error("Failed to send message to token {}: {}", token, e.getMessage());
                    failedTokens.add(token);
                }
            }
            
            logger.info("Completed sending notifications. Success: {}, Failure: {}", 
                    messageIds.size(), failedTokens.size());
            
            return messageIds;
        } catch (Exception e) {
            logger.error("Error in multicast notification process: {}", e.getMessage(), e);
            throw new RuntimeException("Error in multicast notification process", e);
        }
    }
    
    /**
     * Send a notification asynchronously
     * 
     * @param notification Notification data
     * @param token Device token
     * @return Message ID
     */
    public String sendNotificationAsync(PatientNotificationDto notification, String token) {
        try {
            Message message = createMessage(notification, token);
            ApiFuture<String> future = FirebaseMessaging.getInstance().sendAsync(message);
            String response = future.get();
            logger.info("Successfully sent async message: {}", response);
            return response;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error sending async Firebase notification: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error sending async Firebase notification", e);
        }
    }
    
    /**
     * Create a message for a single device
     * 
     * @param notification Notification data
     * @param token Device token
     * @return Message
     */
    private Message createMessage(PatientNotificationDto notification, String token) {
        return Message.builder()
                .setNotification(createNotification(notification))
                .putAllData(createDataPayload(notification))
                .setToken(token)
                .setApnsConfig(createApnsConfig())
                .setAndroidConfig(createAndroidConfig())
                .setWebpushConfig(createWebpushConfig())
                .build();
    }
    
    /**
     * Create a multicast message for multiple devices
     * 
     * @param notification Notification data
     * @param tokens List of device tokens
     * @return MulticastMessage
     */
    private MulticastMessage createMulticastMessage(PatientNotificationDto notification, List<String> tokens) {
        return MulticastMessage.builder()
                .setNotification(createNotification(notification))
                .putAllData(createDataPayload(notification))
                .addAllTokens(tokens)
                .setApnsConfig(createApnsConfig())
                .setAndroidConfig(createAndroidConfig())
                .setWebpushConfig(createWebpushConfig())
                .build();
    }
    
    /**
     * Create notification object
     * 
     * @param notification Notification data
     * @return Notification
     */
    private Notification createNotification(PatientNotificationDto notification) {
        // Use the title and body fields if they are set, otherwise fall back to generated values
        String title = notification.getTitle();
        String body = notification.getBody();
        
        // Fallback if title or body is not set
        if (title == null || title.isEmpty()) {
            title = getNotificationTitle(notification.getStatus());
        }
        
        if (body == null || body.isEmpty()) {
            body = notification.getNotificationMessage();
        }
        
        logger.info("Creating notification with title: '{}', body: '{}'", title, body);
        
        return Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();
    }
    
    /**
     * Create data payload
     * 
     * @param notification Notification data
     * @return Data payload
     */
    private java.util.Map<String, String> createDataPayload(PatientNotificationDto notification) {
        java.util.Map<String, String> data = new java.util.HashMap<>();
        data.put("patientId", notification.getPatientId().toString());
        data.put("status", notification.getStatus());
        
        if (notification.getQueuePosition() != null) {
            data.put("queuePosition", notification.getQueuePosition().toString());
        }
        
        if (notification.getQueueId() != null) {
            data.put("queueId", notification.getQueueId().toString());
        }
        
        if (notification.getQueueName() != null) {
            data.put("queueName", notification.getQueueName());
        }
        
        return data;
    }
    
    /**
     * Create APNS (iOS) configuration
     * 
     * @return ApnsConfig
     */
    private ApnsConfig createApnsConfig() {
        return ApnsConfig.builder()
                .setAps(Aps.builder()
                        .setSound("default")
                        .setContentAvailable(true)
                        .setMutableContent(true)
                        .build())
                .build();
    }
    
    /**
     * Create Android configuration
     * 
     * @return AndroidConfig
     */
    private AndroidConfig createAndroidConfig() {
        return AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder()
                        .setSound("default")
                        .setClickAction("OPEN_PATIENT_STATUS")
                        .build())
                .build();
    }
    
    /**
     * Create Webpush configuration for web push notifications
     * 
     * @return WebpushConfig
     */
    private WebpushConfig createWebpushConfig() {
        logger.info("Creating web push configuration");
        
        return WebpushConfig.builder()
                .setNotification(WebpushNotification.builder()
                        .setRequireInteraction(true)
                        .build())
                .build();
    }
    
    /**
     * Get notification title based on patient status
     * 
     * @param status Patient status
     * @return Notification title
     */
    private String getNotificationTitle(String status) {
        switch (status) {
            case "WAITING":
                return "Queue Position Updated";
            case "NOTIFIED":
                return "Get Ready! Your Turn is Coming Up";
            case "SERVING":
                return "It's Your Turn Now!";
            case "SERVED":
                return "Thank You for Your Visit";
            default:
                return "Queue Status Update";
        }
    }
}
