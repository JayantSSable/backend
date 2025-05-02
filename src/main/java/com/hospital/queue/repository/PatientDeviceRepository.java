package com.hospital.queue.repository;

import com.hospital.queue.model.PatientDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing patient device tokens
 */
@Repository
public interface PatientDeviceRepository extends JpaRepository<PatientDevice, Long> {
    
    /**
     * Find all device tokens for a specific patient
     * 
     * @param patientId Patient ID
     * @return List of patient devices
     */
    List<PatientDevice> findByPatientId(Long patientId);
    
    /**
     * Find a device by patient ID and token
     * 
     * @param patientId Patient ID
     * @param deviceToken Device token
     * @return Optional containing the device if found
     */
    Optional<PatientDevice> findByPatientIdAndDeviceToken(Long patientId, String deviceToken);
    
    /**
     * Delete all devices for a specific patient
     * 
     * @param patientId Patient ID
     */
    void deleteByPatientId(Long patientId);
}
