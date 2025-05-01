package com.hospital.queue.dto;

import com.hospital.queue.model.Patient.PatientStatus;

import javax.validation.constraints.NotNull;


public class PatientStatusUpdateDTO {
    
    @NotNull(message = "Patient status is required")
    private PatientStatus status;
    
    public PatientStatusUpdateDTO() {
    }
    
    public PatientStatusUpdateDTO(PatientStatus status) {
        this.status = status;
    }
    
    public PatientStatus getStatus() {
        return status;
    }
    
    public void setStatus(PatientStatus status) {
        this.status = status;
    }
}
