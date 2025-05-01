package com.hospital.queue.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


public class QueueDTO {
    
    private Long id;
    
    @NotBlank(message = "Queue name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Department ID is required")
    private Long departmentId;
    
    private String qrCodeId;
    
    public QueueDTO() {
    }
    
    public QueueDTO(Long id, String name, String description, Long departmentId, String qrCodeId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.departmentId = departmentId;
        this.qrCodeId = qrCodeId;
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Long getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
    
    public String getQrCodeId() {
        return qrCodeId;
    }
    
    public void setQrCodeId(String qrCodeId) {
        this.qrCodeId = qrCodeId;
    }
}
