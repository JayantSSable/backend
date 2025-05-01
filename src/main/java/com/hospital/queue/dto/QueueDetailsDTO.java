package com.hospital.queue.dto;

import com.hospital.queue.model.Patient;

import java.util.List;
import java.util.stream.Collectors;


public class QueueDetailsDTO {
    
    private Long id;
    private String name;
    private String description;
    private Long departmentId;
    private String departmentName;
    private String qrCodeId;
    private String qrCodeImage;
    private PatientDetailsDTO currentPatient;
    private List<PatientDetailsDTO> waitingPatients;
    private List<PatientDetailsDTO> servedPatients;
    private List<PatientDetailsDTO> cancelledPatients;
    private int waitingCount;
    private int servedCount;
    
    public QueueDetailsDTO() {
    }
    
    public QueueDetailsDTO(Long id, String name, String description, Long departmentId, String departmentName, 
                          String qrCodeId, String qrCodeImage, PatientDetailsDTO currentPatient, 
                          List<PatientDetailsDTO> waitingPatients, List<PatientDetailsDTO> servedPatients, 
                          List<PatientDetailsDTO> cancelledPatients, int waitingCount, int servedCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.qrCodeId = qrCodeId;
        this.qrCodeImage = qrCodeImage;
        this.currentPatient = currentPatient;
        this.waitingPatients = waitingPatients;
        this.servedPatients = servedPatients;
        this.cancelledPatients = cancelledPatients;
        this.waitingCount = waitingCount;
        this.servedCount = servedCount;
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
    
    public String getDepartmentName() {
        return departmentName;
    }
    
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
    
    public String getQrCodeId() {
        return qrCodeId;
    }
    
    public void setQrCodeId(String qrCodeId) {
        this.qrCodeId = qrCodeId;
    }
    
    public String getQrCodeImage() {
        return qrCodeImage;
    }
    
    public void setQrCodeImage(String qrCodeImage) {
        this.qrCodeImage = qrCodeImage;
    }
    
    public PatientDetailsDTO getCurrentPatient() {
        return currentPatient;
    }
    
    public void setCurrentPatient(Patient currentPatient) {
        if (currentPatient != null) {
            this.currentPatient = PatientDetailsDTO.fromPatient(currentPatient);
        } else {
            this.currentPatient = null;
        }
    }
    
    public List<PatientDetailsDTO> getWaitingPatients() {
        return waitingPatients;
    }
    
    public void setWaitingPatients(List<Patient> waitingPatients) {
        if (waitingPatients != null) {
            this.waitingPatients = waitingPatients.stream()
                .map(PatientDetailsDTO::fromPatient)
                .collect(Collectors.toList());
        } else {
            this.waitingPatients = null;
        }
    }
    
    public List<PatientDetailsDTO> getServedPatients() {
        return servedPatients;
    }
    
    public void setServedPatients(List<Patient> servedPatients) {
        if (servedPatients != null) {
            this.servedPatients = servedPatients.stream()
                .map(PatientDetailsDTO::fromPatient)
                .collect(Collectors.toList());
        } else {
            this.servedPatients = null;
        }
    }
    
    public List<PatientDetailsDTO> getCancelledPatients() {
        return cancelledPatients;
    }
    
    public void setCancelledPatients(List<Patient> cancelledPatients) {
        if (cancelledPatients != null) {
            this.cancelledPatients = cancelledPatients.stream()
                .map(PatientDetailsDTO::fromPatient)
                .collect(Collectors.toList());
        } else {
            this.cancelledPatients = null;
        }
    }
    
    public int getWaitingCount() {
        return waitingCount;
    }
    
    public void setWaitingCount(int waitingCount) {
        this.waitingCount = waitingCount;
    }
    
    public int getServedCount() {
        return servedCount;
    }
    
    public void setServedCount(int servedCount) {
        this.servedCount = servedCount;
    }
}
