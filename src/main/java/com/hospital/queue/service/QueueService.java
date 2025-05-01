package com.hospital.queue.service;

import com.google.zxing.WriterException;
import com.hospital.queue.dto.QueueDTO;
import com.hospital.queue.dto.QueueDetailsDTO;
import com.hospital.queue.exception.ResourceNotFoundException;
import com.hospital.queue.model.Department;
import com.hospital.queue.model.Patient;
import com.hospital.queue.model.Queue;
import com.hospital.queue.repository.DepartmentRepository;
import com.hospital.queue.repository.PatientRepository;
import com.hospital.queue.repository.QueueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QueueService {

    private final QueueRepository queueRepository;
    private final DepartmentRepository departmentRepository;
    private final PatientRepository patientRepository;
    private final QRCodeService qrCodeService;
    private final NotificationService notificationService;
    
    public QueueService(QueueRepository queueRepository, 
                      DepartmentRepository departmentRepository,
                      PatientRepository patientRepository,
                      QRCodeService qrCodeService,
                      NotificationService notificationService) {
        this.queueRepository = queueRepository;
        this.departmentRepository = departmentRepository;
        this.patientRepository = patientRepository;
        this.qrCodeService = qrCodeService;
        this.notificationService = notificationService;
    }

    public List<QueueDTO> getAllQueues() {
        return queueRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<QueueDTO> getQueuesByDepartment(Long departmentId) {
        return queueRepository.findByDepartmentId(departmentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public QueueDetailsDTO getQueueDetails(Long queueId) {
        if (queueId == null) {
            System.err.println("getQueueDetails called with null queueId");
            throw new IllegalArgumentException("Queue ID cannot be null");
        }
        
        System.out.println("Fetching queue details for ID: " + queueId);
        
        Queue queue = queueRepository.findById(queueId)
                .orElseThrow(() -> {
                    System.err.println("Queue not found with id: " + queueId);
                    return new ResourceNotFoundException("Queue not found with id: " + queueId);
                });
        
        System.out.println("Found queue: " + queue.getName() + " (ID: " + queue.getId() + ")");
        
        QueueDetailsDTO queueDetails = new QueueDetailsDTO();
        queueDetails.setId(queue.getId());
        queueDetails.setName(queue.getName());
        queueDetails.setDescription(queue.getDescription());
        queueDetails.setDepartmentId(queue.getDepartment().getId());
        queueDetails.setDepartmentName(queue.getDepartment().getName());
        queueDetails.setQrCodeId(queue.getQrCodeId());
        
        try {
            queueDetails.setQrCodeImage(qrCodeService.generateQRCodeImage(queue.getQrCodeId(), 250, 250));
        } catch (WriterException | IOException e) {
            queueDetails.setQrCodeImage(null);
        }
        
        // Get all patients in this queue, grouped by status
        List<Patient> allPatients = patientRepository.findByQueueIdOrderByQueuePosition(queueId);
        
        // Get current serving patient
        List<Patient> servingPatients = allPatients.stream()
                .filter(p -> p.getStatus() == Patient.PatientStatus.SERVING)
                .collect(Collectors.toList());
                
        if (!servingPatients.isEmpty()) {
            queueDetails.setCurrentPatient(servingPatients.get(0));
        }
        
        // Get waiting and notified patients (these should be shown in the waiting list)
        List<Patient> waitingPatients = allPatients.stream()
                .filter(p -> p.getStatus() == Patient.PatientStatus.WAITING || 
                             p.getStatus() == Patient.PatientStatus.NOTIFIED)
                .collect(Collectors.toList());
        queueDetails.setWaitingPatients(waitingPatients);
        
        // Get served patients
        List<Patient> servedPatients = allPatients.stream()
                .filter(p -> p.getStatus() == Patient.PatientStatus.SERVED)
                .collect(Collectors.toList());
        queueDetails.setServedPatients(servedPatients);
        
        // Get cancelled patients
        List<Patient> cancelledPatients = allPatients.stream()
                .filter(p -> p.getStatus() == Patient.PatientStatus.CANCELLED)
                .collect(Collectors.toList());
        queueDetails.setCancelledPatients(cancelledPatients);
        
        return queueDetails;
    }

    @Transactional
    public QueueDTO createQueue(QueueDTO queueDTO) {
        Department department = departmentRepository.findById(queueDTO.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + queueDTO.getDepartmentId()));
        
        Queue queue = new Queue();
        queue.setName(queueDTO.getName());
        queue.setDescription(queueDTO.getDescription());
        queue.setDepartment(department);
        queue.setQrCodeId(qrCodeService.generateQRCodeId());
        queue.setCreatedAt(LocalDateTime.now());
        
        Queue savedQueue = queueRepository.save(queue);
        
        // Broadcast queue creation event
        notificationService.broadcastQueueUpdate(savedQueue.getId());
        
        return convertToDTO(savedQueue);
    }

    @Transactional
    public QueueDTO updateQueue(Long id, QueueDTO queueDTO) {
        Queue queue = queueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Queue not found with id: " + id));
        
        Department department = departmentRepository.findById(queueDTO.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + queueDTO.getDepartmentId()));
        
        queue.setName(queueDTO.getName());
        queue.setDescription(queueDTO.getDescription());
        queue.setDepartment(department);
        queue.setUpdatedAt(LocalDateTime.now());
        
        Queue updatedQueue = queueRepository.save(queue);
        return convertToDTO(updatedQueue);
    }

    @Transactional
    public void deleteQueue(Long id) {
        Queue queue = queueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Queue not found with id: " + id));
        
        // First, delete all patients in this queue
        List<Patient> patients = patientRepository.findByQueueIdOrderByQueuePosition(id);
        if (!patients.isEmpty()) {
            System.out.println("Deleting " + patients.size() + " patients from queue " + id);
            patientRepository.deleteAll(patients);
        }
        
        // Then delete the queue itself
        queueRepository.delete(queue);
        
        System.out.println("Queue " + id + " deleted successfully");
    }

    private QueueDTO convertToDTO(Queue queue) {
        QueueDTO dto = new QueueDTO();
        dto.setId(queue.getId());
        dto.setName(queue.getName());
        dto.setDescription(queue.getDescription());
        dto.setDepartmentId(queue.getDepartment().getId());
        dto.setQrCodeId(queue.getQrCodeId());
        return dto;
    }
}
