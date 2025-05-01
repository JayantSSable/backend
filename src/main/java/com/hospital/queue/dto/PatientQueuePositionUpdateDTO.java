package com.hospital.queue.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class PatientQueuePositionUpdateDTO {
    
    @NotNull(message = "Queue position is required")
    @Min(value = 1, message = "Queue position must be at least 1")
    private Integer queuePosition;
    
    public PatientQueuePositionUpdateDTO() {
    }
    
    public PatientQueuePositionUpdateDTO(Integer queuePosition) {
        this.queuePosition = queuePosition;
    }
    
    public Integer getQueuePosition() {
        return queuePosition;
    }
    
    public void setQueuePosition(Integer queuePosition) {
        this.queuePosition = queuePosition;
    }
}
