package com.hospital.queue.dto;

import javax.validation.constraints.NotBlank;


public class DepartmentDTO {
    
    private Long id;
    
    @NotBlank(message = "Department name is required")
    private String name;
    
    private String description;
    
    private Long hospitalId;
    
    private String hospitalName;
    
    public DepartmentDTO() {
    }
    
    public DepartmentDTO(Long id, String name, String description, Long hospitalId, String hospitalName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.hospitalId = hospitalId;
        this.hospitalName = hospitalName;
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
    
    public Long getHospitalId() {
        return hospitalId;
    }
    
    public void setHospitalId(Long hospitalId) {
        this.hospitalId = hospitalId;
    }
    
    public String getHospitalName() {
        return hospitalName;
    }
    
    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }
}
