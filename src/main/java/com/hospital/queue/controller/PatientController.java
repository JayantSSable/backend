package com.hospital.queue.controller;

import com.hospital.queue.dto.PatientDTO;
import com.hospital.queue.dto.PatientQueuePositionUpdateDTO;
import com.hospital.queue.dto.PatientStatusUpdateDTO;
import com.hospital.queue.service.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/patients")
// CORS is configured globally in WebConfig
public class PatientController {

    private final PatientService patientService;
    
    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public ResponseEntity<List<PatientDTO>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientDTO> getPatientById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @PostMapping("/register")
    public ResponseEntity<PatientDTO> registerPatient(@Valid @RequestBody PatientDTO patientDTO) {
        PatientDTO registeredPatient = patientService.registerPatient(patientDTO);
        return new ResponseEntity<>(registeredPatient, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PatientDTO> updatePatientStatus(
            @PathVariable Long id,
            @Valid @RequestBody PatientStatusUpdateDTO statusUpdateDTO) {
        return ResponseEntity.ok(patientService.updatePatientStatus(id, statusUpdateDTO));
    }
    
    @PatchMapping("/{id}/position")
    public ResponseEntity<PatientDTO> updatePatientQueuePosition(
            @PathVariable Long id,
            @Valid @RequestBody PatientQueuePositionUpdateDTO positionUpdateDTO) {
        return ResponseEntity.ok(patientService.updatePatientQueuePosition(id, positionUpdateDTO.getQueuePosition()));
    }
}
