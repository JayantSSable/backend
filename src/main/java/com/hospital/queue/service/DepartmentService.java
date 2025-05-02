package com.hospital.queue.service;

import com.hospital.queue.dto.DepartmentDTO;
import com.hospital.queue.exception.ResourceAlreadyExistsException;
import com.hospital.queue.exception.ResourceNotFoundException;
import com.hospital.queue.model.Department;
import com.hospital.queue.model.Hospital;
import com.hospital.queue.repository.DepartmentRepository;
import com.hospital.queue.repository.HospitalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final HospitalRepository hospitalRepository;
    
    public DepartmentService(DepartmentRepository departmentRepository, HospitalRepository hospitalRepository) {
        this.departmentRepository = departmentRepository;
        this.hospitalRepository = hospitalRepository;
    }

    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<DepartmentDTO> getDepartmentsByHospitalId(Long hospitalId) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with id: " + hospitalId));
        
        return hospital.getDepartments().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public DepartmentDTO getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return convertToDTO(department);
    }

    @Transactional
    public DepartmentDTO createDepartment(DepartmentDTO departmentDTO) {
        Hospital hospital = hospitalRepository.findById(departmentDTO.getHospitalId())
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with id: " + departmentDTO.getHospitalId()));
        
        Department department = new Department();
        department.setName(departmentDTO.getName());
        department.setDescription(departmentDTO.getDescription());
        department.setHospital(hospital);
        
        Department savedDepartment = departmentRepository.save(department);
        return convertToDTO(savedDepartment);
    }

    @Transactional
    public DepartmentDTO updateDepartment(Long id, DepartmentDTO departmentDTO) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        
        if (departmentDTO.getHospitalId() != null && 
            !departmentDTO.getHospitalId().equals(department.getHospital().getId())) {
            Hospital hospital = hospitalRepository.findById(departmentDTO.getHospitalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with id: " + departmentDTO.getHospitalId()));
            department.setHospital(hospital);
        }
        
        department.setName(departmentDTO.getName());
        department.setDescription(departmentDTO.getDescription());
        
        Department updatedDepartment = departmentRepository.save(department);
        return convertToDTO(updatedDepartment);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department not found with id: " + id);
        }
        departmentRepository.deleteById(id);
    }

    private DepartmentDTO convertToDTO(Department department) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setDescription(department.getDescription());
        
        if (department.getHospital() != null) {
            dto.setHospitalId(department.getHospital().getId());
            dto.setHospitalName(department.getHospital().getName());
        }
        
        return dto;
    }
}
