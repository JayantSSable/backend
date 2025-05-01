package com.hospital.queue.dto;

import com.hospital.queue.model.Patient.PatientStatus;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;


public class PatientDTO {
    
    private Long id;
    
    @NotBlank(message = "Patient name is required")
    private String name;
    
    private String phoneNumber;
    
    private String email;
    
    private Long queueId;
    
    private String queueName;
    
    private String qrCodeId;
    
    private PatientStatus status;
    
    private Integer queuePosition;
    
    private LocalDateTime joinedAt;
    
    private LocalDateTime servedAt;
    
    private LocalDateTime notifiedAt;
    
    public PatientDTO() {
    }
    
    public PatientDTO(Long id, String name, String phoneNumber, String email, Long queueId, String queueName, String qrCodeId, PatientStatus status, Integer queuePosition, LocalDateTime joinedAt, LocalDateTime servedAt, LocalDateTime notifiedAt) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.queueId = queueId;
        this.queueName = queueName;
        this.qrCodeId = qrCodeId;
        this.status = status;
        this.queuePosition = queuePosition;
        this.joinedAt = joinedAt;
        this.servedAt = servedAt;
        this.notifiedAt = notifiedAt;
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
    
    public String getQrCodeId() {
        return qrCodeId;
    }
    
    public void setQrCodeId(String qrCodeId) {
        this.qrCodeId = qrCodeId;
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
