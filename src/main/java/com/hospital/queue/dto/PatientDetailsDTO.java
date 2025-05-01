package com.hospital.queue.dto;

import com.hospital.queue.model.Patient;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Patient details without circular references
 */
public class PatientDetailsDTO {
    
    private Long id;
    private String name;
    private String phoneNumber;
    private String email;
    private Long queueId;
    private String queueName;
    private Patient.PatientStatus status;
    private Integer queuePosition;
    private LocalDateTime joinedAt;
    private LocalDateTime servedAt;
    private LocalDateTime notifiedAt;
    
    public PatientDetailsDTO() {
    }
    
    /**
     * Create a DTO from a Patient entity
     */
    public static PatientDetailsDTO fromPatient(Patient patient) {
        PatientDetailsDTO dto = new PatientDetailsDTO();
        dto.setId(patient.getId());
        dto.setName(patient.getName());
        dto.setPhoneNumber(patient.getPhoneNumber());
        dto.setEmail(patient.getEmail());
        
        if (patient.getQueue() != null) {
            dto.setQueueId(patient.getQueue().getId());
            dto.setQueueName(patient.getQueue().getName());
        }
        
        dto.setStatus(patient.getStatus());
        dto.setQueuePosition(patient.getQueuePosition());
        dto.setJoinedAt(patient.getJoinedAt());
        dto.setServedAt(patient.getServedAt());
        dto.setNotifiedAt(patient.getNotifiedAt());
        
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public Patient.PatientStatus getStatus() {
        return status;
    }

    public void setStatus(Patient.PatientStatus status) {
        this.status = status;
    }

    public Integer getQueuePosition() {
        return queuePosition;
    }

    public void setQueuePosition(Integer queuePosition) {
        this.queuePosition = queuePosition;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public LocalDateTime getServedAt() {
        return servedAt;
    }

    public void setServedAt(LocalDateTime servedAt) {
        this.servedAt = servedAt;
    }

    public LocalDateTime getNotifiedAt() {
        return notifiedAt;
    }

    public void setNotifiedAt(LocalDateTime notifiedAt) {
        this.notifiedAt = notifiedAt;
    }
}
