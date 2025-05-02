package com.hospital.queue.service;

import com.hospital.queue.dto.DepartmentDTO;
import com.hospital.queue.dto.HospitalDTO;
import com.hospital.queue.exception.ResourceNotFoundException;
import com.hospital.queue.model.Department;
import com.hospital.queue.model.Hospital;
import com.hospital.queue.repository.HospitalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HospitalService {

    private final HospitalRepository hospitalRepository;
    private final DepartmentService departmentService;

    @Autowired
    public HospitalService(HospitalRepository hospitalRepository, DepartmentService departmentService) {
        this.hospitalRepository = hospitalRepository;
        this.departmentService = departmentService;
    }

    public List<HospitalDTO> getAllHospitals() {
        return hospitalRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public HospitalDTO getHospitalById(Long id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with id: " + id));
        return convertToDTO(hospital);
    }

    @Transactional
    public HospitalDTO createHospital(HospitalDTO hospitalDTO) {
        Hospital hospital = new Hospital();
        hospital.setName(hospitalDTO.getName());
        hospital.setAddress(hospitalDTO.getAddress());
        hospital.setContactNumber(hospitalDTO.getContactNumber());
        hospital.setEmail(hospitalDTO.getEmail());
        hospital.setDescription(hospitalDTO.getDescription());
        
        Hospital savedHospital = hospitalRepository.save(hospital);
        return convertToDTO(savedHospital);
    }

    @Transactional
    public HospitalDTO updateHospital(Long id, HospitalDTO hospitalDTO) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with id: " + id));
        
        hospital.setName(hospitalDTO.getName());
        hospital.setAddress(hospitalDTO.getAddress());
        hospital.setContactNumber(hospitalDTO.getContactNumber());
        hospital.setEmail(hospitalDTO.getEmail());
        hospital.setDescription(hospitalDTO.getDescription());
        hospital.setUpdatedAt(LocalDateTime.now());
        
        Hospital updatedHospital = hospitalRepository.save(hospital);
        return convertToDTO(updatedHospital);
    }

    @Transactional
    public void deleteHospital(Long id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with id: " + id));
        
        hospitalRepository.delete(hospital);
    }

    // Helper method to convert Hospital entity to DTO
    private HospitalDTO convertToDTO(Hospital hospital) {
        HospitalDTO dto = new HospitalDTO();
        dto.setId(hospital.getId());
        dto.setName(hospital.getName());
        dto.setAddress(hospital.getAddress());
        dto.setContactNumber(hospital.getContactNumber());
        dto.setEmail(hospital.getEmail());
        dto.setDescription(hospital.getDescription());
        dto.setCreatedAt(hospital.getCreatedAt());
        dto.setUpdatedAt(hospital.getUpdatedAt());
        
        // Convert departments to DTOs (but avoid infinite recursion)
        if (hospital.getDepartments() != null) {
            List<DepartmentDTO> departmentDTOs = hospital.getDepartments().stream()
                    .map(this::convertDepartmentToDTO)
                    .collect(Collectors.toList());
            dto.setDepartments(departmentDTOs);
        }
        
        return dto;
    }
    
    // Helper method to convert Department entity to DTO (simplified to avoid recursion)
    private DepartmentDTO convertDepartmentToDTO(Department department) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setDescription(department.getDescription());
        // Don't include hospital or queues to avoid recursion
        return dto;
    }
}
