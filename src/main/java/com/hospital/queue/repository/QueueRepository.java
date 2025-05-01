package com.hospital.queue.repository;

import com.hospital.queue.model.Queue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QueueRepository extends JpaRepository<Queue, Long> {
    List<Queue> findByDepartmentId(Long departmentId);
    Optional<Queue> findByQrCodeId(String qrCodeId);
}
