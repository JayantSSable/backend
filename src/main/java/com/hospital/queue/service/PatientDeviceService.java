package com.hospital.queue.service;

import com.hospital.queue.dto.DeviceRegistrationDto;
import com.hospital.queue.dto.PatientNotificationDto;
import com.hospital.queue.exception.ResourceNotFoundException;
import com.hospital.queue.model.Patient;
import com.hospital.queue.model.PatientDevice;
import com.hospital.queue.model.Queue;
import com.hospital.queue.repository.PatientDeviceRepository;
import com.hospital.queue.repository.PatientRepository;
import com.hospital.queue.repository.QueueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing patient device tokens and notifications
 */
@Service
public class PatientDeviceService {
    
    private static final Logger logger = LoggerFactory.getLogger(PatientDeviceService.class);
    
    private final PatientDeviceRepository patientDeviceRepository;
    private final PatientRepository patientRepository;
    private final QueueRepository queueRepository;
    private final FirebaseMessagingService firebaseMessagingService;
    
    @Autowired
    public PatientDeviceService(
            PatientDeviceRepository patientDeviceRepository,
            PatientRepository patientRepository,
            QueueRepository queueRepository,
            FirebaseMessagingService firebaseMessagingService) {
        this.patientDeviceRepository = patientDeviceRepository;
        this.patientRepository = patientRepository;
        this.queueRepository = queueRepository;
        this.firebaseMessagingService = firebaseMessagingService;
    }
    
    /**
     * Register a device token for a patient
     * 
     * @param registrationDto Device registration data
     * @return The registered device
     */
    @Transactional
    public PatientDevice registerDevice(DeviceRegistrationDto registrationDto) {
        logger.info("Starting device registration process for patient ID: {}", registrationDto.getPatientId());
        logger.info("Device token to register: {}", registrationDto.getDeviceToken());
        
        try {
            // Verify patient exists
            logger.info("Verifying patient exists with ID: {}", registrationDto.getPatientId());
            patientRepository.findById(registrationDto.getPatientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + registrationDto.getPatientId()));
            
            // Check if device token already exists for this patient
            logger.info("Checking if device token already exists for patient: {}", registrationDto.getPatientId());
            return patientDeviceRepository.findByPatientIdAndDeviceToken(
                    registrationDto.getPatientId(), 
                    registrationDto.getDeviceToken())
                    .orElseGet(() -> {
                        // Create new device token
                        logger.info("Device token not found, creating new entry for patient: {}", registrationDto.getPatientId());
                        PatientDevice device = new PatientDevice();
                        device.setPatientId(registrationDto.getPatientId());
                        device.setDeviceToken(registrationDto.getDeviceToken());
                        
                        logger.info("Saving new device token for patient: {}", registrationDto.getPatientId());
                        PatientDevice savedDevice = patientDeviceRepository.save(device);
                        logger.info("Device token saved with ID: {}", savedDevice.getId());
                        return savedDevice;
                    });
        } catch (Exception e) {
            logger.error("Error in registerDevice: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Get all device tokens for a patient
     * 
     * @param patientId Patient ID
     * @return List of device tokens
     */
    public List<String> getDeviceTokensForPatient(Long patientId) {
        return patientDeviceRepository.findByPatientId(patientId)
                .stream()
                .map(PatientDevice::getDeviceToken)
                .collect(Collectors.toList());
    }
    
    /**
     * Delete a device token
     * 
     * @param deviceId Device ID
     */
    @Transactional
    public void deleteDevice(Long deviceId) {
        patientDeviceRepository.deleteById(deviceId);
    }
    
    /**
     * Delete all device tokens for a patient
     * 
     * @param patientId Patient ID
     */
    @Transactional
    public void deleteAllDevicesForPatient(Long patientId) {
        patientDeviceRepository.deleteByPatientId(patientId);
    }
    
    /**
     * Send a notification to a patient
     * 
     * @param patientId Patient ID
     * @param status New status
     * @return Success status
     */
    public boolean sendStatusNotification(Long patientId, String status) {
        try {
            // Get patient details
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));
            
            // Get queue details
            Queue queue = queueRepository.findById(patient.getQueue().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Queue not found with id: " + patient.getQueue().getId()));
            
            // Get device tokens
            List<String> deviceTokens = getDeviceTokensForPatient(patientId);
            
            if (deviceTokens.isEmpty()) {
                logger.warn("No device tokens found for patient: {}", patientId);
                return false;
            }
            
            // Create notification with explicit title and body
            PatientNotificationDto notification = new PatientNotificationDto(
                    patient.getId(),
                    status,
                    patient.getQueuePosition(),
                    patient.getQueue().getId(),
                    null,
                    queue.getName()
            );
            
            // Set explicit title and body for FCM
            String title = "TEST".equals(status) ? 
                "Test Notification" : 
                "Queue Update: " + queue.getName();
                
            String body = "TEST".equals(status) ? 
                "This is a test notification from Hospital Queue System" : 
                "Your status has been updated to: " + status;
                
            notification.setTitle(title);
            notification.setBody(body);
            
            logger.info("Sending notification with title: '{}', body: '{}'", title, body);
            
            // Send notification
            firebaseMessagingService.sendMulticastNotification(notification, deviceTokens);
            logger.info("Sent status notification to patient {} with {} device(s)", patientId, deviceTokens.size());
            
            return true;
        } catch (Exception e) {
            logger.error("Error sending notification to patient {}: {}", patientId, e.getMessage(), e);
            return false;
        }
    }
}
