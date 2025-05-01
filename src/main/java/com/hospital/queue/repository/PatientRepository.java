package com.hospital.queue.repository;

import com.hospital.queue.model.Patient;
import com.hospital.queue.model.Patient.PatientStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    // Find all patients in a queue ordered by position
    List<Patient> findByQueueIdOrderByQueuePosition(Long queueId);
    
    // Find patients with specific status
    List<Patient> findByQueueIdAndStatusOrderByQueuePosition(Long queueId, PatientStatus status);
    
    // Find patients with any of the specified statuses
    List<Patient> findByQueueIdAndStatusInOrderByQueuePosition(Long queueId, List<PatientStatus> statuses);
    
    @Query("SELECT COUNT(p) FROM Patient p WHERE p.queue.id = ?1 AND p.status = ?2")
    int countByQueueIdAndStatus(Long queueId, PatientStatus status);
    
    @Query("SELECT MAX(p.queuePosition) FROM Patient p WHERE p.queue.id = ?1")
    Integer findMaxQueuePositionByQueueId(Long queueId);
}
