package com.hospital.queue.controller;

import com.hospital.queue.dto.PatientDTO;
import com.hospital.queue.service.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/registration")
@CrossOrigin(origins = "*")
public class RegistrationController {

    private final PatientService patientService;

    public RegistrationController(PatientService patientService) {
        this.patientService = patientService;
    }

    /**
     * Register a patient to a queue
     * This endpoint is kept for backward compatibility
     */
    @PostMapping("/register")
    public ResponseEntity<PatientDTO> registerPatient(@Valid @RequestBody PatientDTO patientDTO) {
        // Register the patient
        PatientDTO registeredPatient = patientService.registerPatient(patientDTO);
        return new ResponseEntity<>(registeredPatient, HttpStatus.CREATED);
    }
}
