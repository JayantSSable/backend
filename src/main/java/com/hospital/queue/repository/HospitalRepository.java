package com.hospital.queue.repository;

import com.hospital.queue.model.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Long> {
    // Add custom query methods if needed
}
