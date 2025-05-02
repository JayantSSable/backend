package com.hospital.queue.controller;

import com.hospital.queue.dto.HospitalDTO;
import com.hospital.queue.service.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
// CORS is configured globally in WebConfig
public class HospitalController {

    private final HospitalService hospitalService;

    @Autowired
    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    @GetMapping
    public ResponseEntity<List<HospitalDTO>> getAllHospitals() {
        List<HospitalDTO> hospitals = hospitalService.getAllHospitals();
        return ResponseEntity.ok(hospitals);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HospitalDTO> getHospitalById(@PathVariable Long id) {
        HospitalDTO hospital = hospitalService.getHospitalById(id);
        return ResponseEntity.ok(hospital);
    }

    @PostMapping
    public ResponseEntity<HospitalDTO> createHospital(@RequestBody HospitalDTO hospitalDTO) {
        HospitalDTO createdHospital = hospitalService.createHospital(hospitalDTO);
        return new ResponseEntity<>(createdHospital, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HospitalDTO> updateHospital(@PathVariable Long id, @RequestBody HospitalDTO hospitalDTO) {
        HospitalDTO updatedHospital = hospitalService.updateHospital(id, hospitalDTO);
        return ResponseEntity.ok(updatedHospital);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHospital(@PathVariable Long id) {
        hospitalService.deleteHospital(id);
        return ResponseEntity.noContent().build();
    }
}
