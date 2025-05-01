package com.hospital.queue.service;

import com.hospital.queue.dto.PatientDTO;
import com.hospital.queue.dto.PatientStatusUpdateDTO;
import com.hospital.queue.exception.ResourceNotFoundException;
import com.hospital.queue.model.Patient;
import com.hospital.queue.model.Queue;
import com.hospital.queue.repository.PatientRepository;
import com.hospital.queue.repository.QueueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final QueueRepository queueRepository;
    private final NotificationService notificationService;
    
    public PatientService(PatientRepository patientRepository,
                        QueueRepository queueRepository,
                        NotificationService notificationService) {
        this.patientRepository = patientRepository;
        this.queueRepository = queueRepository;
        this.notificationService = notificationService;
    }

    public List<PatientDTO> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PatientDTO getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
        return convertToDTO(patient);
    }

    @Transactional
    public PatientDTO registerPatient(PatientDTO patientDTO) {
        // Initialize queue to null
        Queue queue = null;
        
        System.out.println("Patient registration request received with data: " + 
                          "queueId=" + patientDTO.getQueueId() + 
                          ", qrCodeId=" + patientDTO.getQrCodeId() + 
                          ", name=" + patientDTO.getName());
        
        // First, try to find the queue by queue ID if provided (this is the most reliable method)
        if (patientDTO.getQueueId() != null) {
            System.out.println("Looking up queue by ID: " + patientDTO.getQueueId());
            queue = queueRepository.findById(patientDTO.getQueueId())
                    .orElse(null);
            
            if (queue != null) {
                System.out.println("Queue found by ID: " + patientDTO.getQueueId() + ", name: " + queue.getName());
            } else {
                System.out.println("Queue not found with ID: " + patientDTO.getQueueId());
            }
        }
        
        // If queue not found by ID and QR code ID is provided, try to find by QR code ID
        if (queue == null && patientDTO.getQrCodeId() != null) {
            // Clean up the QR code ID - remove any "undefined" or empty values
            String qrCodeId = patientDTO.getQrCodeId();
            if (qrCodeId.equals("undefined") || qrCodeId.isEmpty()) {
                System.out.println("QR code ID is undefined or empty, skipping QR code lookup");
            } else {
                System.out.println("Looking up queue by QR code ID: " + qrCodeId);
                
                // Check if this is a direct queue ID reference
                if (qrCodeId.startsWith("direct-")) {
                    String[] parts = qrCodeId.split("-");
                    if (parts.length >= 2) {
                        try {
                            Long directQueueId = Long.parseLong(parts[1]);
                            System.out.println("Extracted direct queue ID: " + directQueueId + " from QR code: " + qrCodeId);
                            queue = queueRepository.findById(directQueueId).orElse(null);
                            
                            if (queue != null) {
                                System.out.println("Queue found by direct ID reference: " + directQueueId + ", name: " + queue.getName());
                            } else {
                                System.out.println("Queue not found with direct ID reference: " + directQueueId);
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Failed to parse queue ID from QR code: " + qrCodeId);
                        }
                    }
                } else {
                    // Try standard QR code lookup
                    queue = queueRepository.findByQrCodeId(qrCodeId).orElse(null);
                    
                    if (queue != null) {
                        System.out.println("Queue found by QR code ID: " + qrCodeId + ", name: " + queue.getName());
                    } else {
                        System.out.println("Queue not found with QR code ID: " + qrCodeId);
                    }
                }
            }
        }
        
        // If queue still not found, throw an exception with detailed information
        if (queue == null) {
            String errorMessage = String.format("Queue not found with QR code: %s or queue ID: %s", 
                patientDTO.getQrCodeId(), patientDTO.getQueueId());
            System.err.println(errorMessage);
            throw new ResourceNotFoundException(errorMessage);
        }
        
        System.out.println("Registering patient to queue: " + queue.getName() + " (ID: " + queue.getId() + ")");
        
        Patient patient = new Patient();
        patient.setName(patientDTO.getName());
        patient.setPhoneNumber(patientDTO.getPhoneNumber());
        patient.setEmail(patientDTO.getEmail());
        patient.setQueue(queue);
        patient.setStatus(Patient.PatientStatus.WAITING);
        
        // Get the next position in the queue
        Integer maxPosition = patientRepository.findMaxQueuePositionByQueueId(queue.getId());
        patient.setQueuePosition(maxPosition != null ? maxPosition + 1 : 1);
        patient.setJoinedAt(LocalDateTime.now());
        
        Patient savedPatient = patientRepository.save(patient);
        
        // Broadcast queue update to all clients
        notificationService.broadcastQueueUpdate(queue.getId());
        
        return convertToDTO(savedPatient);
    }

    @Transactional
    public PatientDTO updatePatientStatus(Long id, PatientStatusUpdateDTO statusUpdateDTO) {
        System.out.println("Updating patient " + id + " status to " + statusUpdateDTO.getStatus());
        
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
        
        Long queueId = patient.getQueue().getId();
        // Store new status
        Patient.PatientStatus newStatus = statusUpdateDTO.getStatus();
        
        // If changing to SERVING status, ensure no other patient is currently being served
        if (newStatus == Patient.PatientStatus.SERVING) {
            // Find any other patients in SERVING status in this queue
            List<Patient> servingPatients = patientRepository.findByQueueIdAndStatusOrderByQueuePosition(
                    queueId, Patient.PatientStatus.SERVING);
            
            // If there are other patients being served, change their status to WAITING
            for (Patient servingPatient : servingPatients) {
                if (!servingPatient.getId().equals(id)) {
                    System.out.println("Changing patient " + servingPatient.getId() + " from SERVING to WAITING");
                    servingPatient.setStatus(Patient.PatientStatus.WAITING);
                    patientRepository.save(servingPatient);
                }
            }
        }
        
        // Update the patient's status
        patient.setStatus(newStatus);
        
        // Handle specific status transitions
        if (newStatus == Patient.PatientStatus.NOTIFIED) {
            patient.setNotifiedAt(LocalDateTime.now());
            notificationService.sendNotification(patient);
        } else if (newStatus == Patient.PatientStatus.SERVING) {
            // If a patient is now being served, notify the next patients in line
            notifyUpcomingPatients(queueId);
        } else if (newStatus == Patient.PatientStatus.SERVED) {
            patient.setServedAt(LocalDateTime.now());
            
            // We no longer automatically call the next patient when one is served
            // This allows the admin to have full control over the queue
        }
        
        Patient updatedPatient = patientRepository.save(patient);
        
        // Broadcast queue update to all clients
        notificationService.broadcastQueueUpdate(queueId);
        
        return convertToDTO(updatedPatient);
    }

    private void notifyUpcomingPatients(Long queueId) {
        List<Patient> waitingPatients = patientRepository.findByQueueIdAndStatusOrderByQueuePosition(
                queueId, Patient.PatientStatus.WAITING);
        
        // Notify the next 2 patients in line
        for (int i = 0; i < Math.min(2, waitingPatients.size()); i++) {
            Patient patient = waitingPatients.get(i);
            patient.setStatus(Patient.PatientStatus.NOTIFIED);
            patient.setNotifiedAt(LocalDateTime.now());
            patientRepository.save(patient);
            notificationService.sendNotification(patient);
        }
        
        // Broadcast queue update to all clients
        notificationService.broadcastQueueUpdate(queueId);
    }

    // Call next patient in queue - can be called manually by admin
    public PatientDTO callNextPatient(Long queueId) {
        // Check if there's already a patient being served
        List<Patient> servingPatients = patientRepository.findByQueueIdAndStatusOrderByQueuePosition(
                queueId, Patient.PatientStatus.SERVING);
        
        // Only move to the next patient if no one is currently being served
        if (servingPatients.isEmpty()) {
            List<Patient> waitingPatients = patientRepository.findByQueueIdAndStatusOrderByQueuePosition(
                    queueId, Patient.PatientStatus.WAITING);
            
            if (!waitingPatients.isEmpty()) {
                Patient nextPatient = waitingPatients.get(0);
                System.out.println("Moving next patient " + nextPatient.getId() + " to SERVING status");
                nextPatient.setStatus(Patient.PatientStatus.SERVING);
                patientRepository.save(nextPatient);
                
                // Send notification to the patient being served
                notificationService.sendNotification(nextPatient);
                
                // Notify upcoming patients
                notifyUpcomingPatients(queueId);
                
                return convertToDTO(nextPatient);
            }
        } else {
            System.out.println("Not moving to next patient because there's already a patient being served");
        }
        
        return null;
    }

    @Transactional
    public PatientDTO updatePatientQueuePosition(Long id, Integer newPosition) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
        
        // Store old position for logging purposes
        Integer oldPosition = patient.getQueuePosition();
        
        // Update the patient's queue position
        patient.setQueuePosition(newPosition);
        
        // Log position change
        System.out.println("Patient queue position changed: " + patient.getName() + 
                          " from " + oldPosition + " to " + newPosition);
        
        Patient updatedPatient = patientRepository.save(patient);
        
        // Broadcast queue update to all clients
        notificationService.broadcastQueueUpdate(patient.getQueue().getId());
        
        // Send notification to the patient about position change
        notificationService.sendNotification(patient);
        
        return convertToDTO(updatedPatient);
    }
    
    private PatientDTO convertToDTO(Patient patient) {
        PatientDTO dto = new PatientDTO();
        dto.setId(patient.getId());
        dto.setName(patient.getName());
        dto.setPhoneNumber(patient.getPhoneNumber());
        dto.setEmail(patient.getEmail());
        dto.setQueueId(patient.getQueue().getId());
        dto.setQueueName(patient.getQueue().getName());
        dto.setQrCodeId(patient.getQueue().getQrCodeId());
        dto.setStatus(patient.getStatus());
        dto.setQueuePosition(patient.getQueuePosition());
        dto.setJoinedAt(patient.getJoinedAt());
        dto.setServedAt(patient.getServedAt());
        dto.setNotifiedAt(patient.getNotifiedAt());
        return dto;
    }
}
