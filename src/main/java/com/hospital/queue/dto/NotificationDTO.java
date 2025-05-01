package com.hospital.queue.dto;

import com.hospital.queue.model.Patient.PatientStatus;

import java.time.LocalDateTime;


public class NotificationDTO {
    
    private Long patientId;
    private String patientName;
    private Long queueId;
    private String queueName;
    private String departmentName;
    private PatientStatus status;
    private Integer queuePosition;
    private LocalDateTime timestamp;
    
    public NotificationDTO() {
    }
    
    public NotificationDTO(Long patientId, String patientName, Long queueId, String queueName, String departmentName, PatientStatus status, Integer queuePosition, LocalDateTime timestamp) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.queueId = queueId;
        this.queueName = queueName;
        this.departmentName = departmentName;
        this.status = status;
        this.queuePosition = queuePosition;
        this.timestamp = timestamp;
    }
    
    public Long getPatientId() {
        return patientId;
    }
    
    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }
    
    public String getPatientName() {
        return patientName;
    }
    
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
    
    public Long getQueueId() {
        return queueId;
    }
    
    public void setQueueId(Long queueId) {
        this.queueId = queueId;
    }
    
    public String getQueueName() {
        return queueName;
    }
    
    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }
    
    public String getDepartmentName() {
        return departmentName;
    }
    
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
    
    public PatientStatus getStatus() {
        return status;
    }
    
    public void setStatus(PatientStatus status) {
        this.status = status;
    }
    
    public Integer getQueuePosition() {
        return queuePosition;
    }
    
    public void setQueuePosition(Integer queuePosition) {
        this.queuePosition = queuePosition;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
