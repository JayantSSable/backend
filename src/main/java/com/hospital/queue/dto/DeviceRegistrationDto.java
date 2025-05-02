package com.hospital.queue.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * DTO for registering a device token for FCM notifications
 */
public class DeviceRegistrationDto {
    
    @NotNull(message = "Patient ID is required")
    private Long patientId;
    
    @NotBlank(message = "Device token is required")
    private String deviceToken;
    
    public DeviceRegistrationDto() {
    }
    
    public DeviceRegistrationDto(Long patientId, String deviceToken) {
        this.patientId = patientId;
        this.deviceToken = deviceToken;
    }
    
    public Long getPatientId() {
        return patientId;
    }
    
    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }
    
    public String getDeviceToken() {
        return deviceToken;
    }
    
    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
}
