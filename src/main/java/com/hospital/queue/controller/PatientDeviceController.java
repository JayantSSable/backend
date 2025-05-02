package com.hospital.queue.controller;

import com.hospital.queue.dto.DeviceRegistrationDto;
import com.hospital.queue.model.PatientDevice;
import com.hospital.queue.service.PatientDeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for managing patient device tokens and notifications
 */
@RestController
@RequestMapping("/api/patients")
// CORS is configured globally in WebConfig
public class PatientDeviceController {
    
    private static final Logger logger = LoggerFactory.getLogger(PatientDeviceController.class);
    
    private final PatientDeviceService patientDeviceService;
    
    @Autowired
    public PatientDeviceController(PatientDeviceService patientDeviceService) {
        this.patientDeviceService = patientDeviceService;
    }
    
    /**
     * Register a device token for FCM notifications
     * 
     * @param registrationDto Device registration data
     * @return Response with registered device
     */
    @PostMapping("/device-token")
    public ResponseEntity<Map<String, Object>> registerDeviceToken(@Valid @RequestBody DeviceRegistrationDto registrationDto) {
        logger.info("Registering device token for patient: {}", registrationDto.getPatientId());
        logger.info("Device token: {}", registrationDto.getDeviceToken());
        
        try {
            PatientDevice device = patientDeviceService.registerDevice(registrationDto);
            
            logger.info("Device token registered successfully with ID: {}", device.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Device token registered successfully");
            response.put("deviceId", device.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error registering device token: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to register device token: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Delete a device token
     * 
     * @param patientId Patient ID
     * @param deviceToken Device token
     * @return Response with success status
     */
    @DeleteMapping("/{patientId}/device-token")
    public ResponseEntity<Map<String, Object>> deleteDeviceToken(
            @PathVariable Long patientId,
            @RequestParam String deviceToken) {
        logger.info("Deleting device token for patient: {}", patientId);
        
        // Find and delete the device token
        patientDeviceService.deleteAllDevicesForPatient(patientId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Device token(s) deleted successfully");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Send a test notification to a patient
     * 
     * @param patientId Patient ID
     * @return Response with success status
     */
    @PostMapping("/{patientId}/test-notification")
    public ResponseEntity<Map<String, Object>> sendTestNotification(@PathVariable Long patientId) {
        logger.info("Sending test notification to patient: {}", patientId);
        
        boolean success = patientDeviceService.sendStatusNotification(patientId, "TEST");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        
        if (success) {
            response.put("message", "Test notification sent successfully");
        } else {
            response.put("message", "Failed to send test notification");
        }
        
        return ResponseEntity.ok(response);
    }
}
