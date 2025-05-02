package com.hospital.queue.dto;

/**
 * DTO for patient notifications sent via Firebase Cloud Messaging
 */
public class PatientNotificationDto {
    
    private Long patientId;
    private String status;
    private Integer queuePosition;
    private Long queueId;
    private String message;
    private String queueName;
    private String title;
    private String body;
    
    public PatientNotificationDto() {
    }
    
    public PatientNotificationDto(Long patientId, String status, Integer queuePosition, Long queueId, String message, String queueName) {
        this.patientId = patientId;
        this.status = status;
        this.queuePosition = queuePosition;
        this.queueId = queueId;
        this.message = message;
        this.queueName = queueName;
        
        // Default title and body based on status and message
        this.title = "Queue Update: " + (queueName != null ? queueName : "");
        this.body = message != null ? message : "Your status has been updated to " + status;
    }
    
    public PatientNotificationDto(Long patientId, String status, Integer queuePosition, Long queueId, String message, String queueName, String title, String body) {
        this.patientId = patientId;
        this.status = status;
        this.queuePosition = queuePosition;
        this.queueId = queueId;
        this.message = message;
        this.queueName = queueName;
        this.title = title;
        this.body = body;
    }
    
    // Getters and setters
    
    public Long getPatientId() {
        return patientId;
    }
    
    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getQueuePosition() {
        return queuePosition;
    }
    
    public void setQueuePosition(Integer queuePosition) {
        this.queuePosition = queuePosition;
    }
    
    public Long getQueueId() {
        return queueId;
    }
    
    public void setQueueId(Long queueId) {
        this.queueId = queueId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getQueueName() {
        return queueName;
    }
    
    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getBody() {
        return body;
    }
    
    public void setBody(String body) {
        this.body = body;
    }
    
    /**
     * Create a notification message based on patient status
     * 
     * @return Formatted notification message
     */
    public String getNotificationMessage() {
        if (message != null && !message.isEmpty()) {
            return message;
        }
        
        switch (status) {
            case "WAITING":
                return "You are now in position " + queuePosition + " in the " + queueName + " queue.";
            case "NOTIFIED":
                return "You will be called soon! Please prepare to be served.";
            case "SERVING":
                return "It is your turn now! Please proceed to the service desk.";
            case "SERVED":
                return "Thank you for your visit!";
            default:
                return "Your status has been updated to " + status;
        }
    }
}
