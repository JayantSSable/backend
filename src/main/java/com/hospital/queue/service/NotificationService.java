package com.hospital.queue.service;

import com.hospital.queue.dto.NotificationDTO;
import com.hospital.queue.model.Patient;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    
    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendNotification(Patient patient) {
        try {
            NotificationDTO notification = new NotificationDTO();
            notification.setPatientId(patient.getId());
            notification.setPatientName(patient.getName());
            notification.setQueueId(patient.getQueue().getId());
            notification.setQueueName(patient.getQueue().getName());
            notification.setDepartmentName(patient.getQueue().getDepartment().getName());
            notification.setStatus(patient.getStatus());
            notification.setQueuePosition(patient.getQueuePosition());
            notification.setTimestamp(LocalDateTime.now());
            
            // Send to specific patient channel
            messagingTemplate.convertAndSend("/topic/patient/" + patient.getId(), notification);
            
            // Send to queue channel
            messagingTemplate.convertAndSend("/topic/queue/" + patient.getQueue().getId(), notification);
        } catch (Exception e) {
            // Log the error but don't let it disrupt the application flow
            System.err.println("WebSocket notification failed: " + e.getMessage());
            // The application can continue without WebSocket notifications
        }
    }

    public void broadcastQueueUpdate(Long queueId) {
        try {
            messagingTemplate.convertAndSend("/topic/queue/" + queueId, "queue-updated");
        } catch (Exception e) {
            // Log the error but don't let it disrupt the application flow
            System.err.println("WebSocket queue broadcast failed: " + e.getMessage());
            // The application can continue without WebSocket notifications
        }
    }
}
