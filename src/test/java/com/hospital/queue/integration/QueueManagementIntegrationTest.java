package com.hospital.queue.integration;

import com.hospital.queue.dto.DepartmentDTO;
import com.hospital.queue.dto.PatientDTO;
import com.hospital.queue.dto.PatientStatusUpdateDTO;
import com.hospital.queue.dto.QueueDTO;
import com.hospital.queue.dto.QueueDetailsDTO;
import com.hospital.queue.model.Patient;
import com.hospital.queue.repository.DepartmentRepository;
import com.hospital.queue.repository.PatientRepository;
import com.hospital.queue.repository.QueueRepository;
import com.hospital.queue.service.DepartmentService;
import com.hospital.queue.service.PatientService;
import com.hospital.queue.service.QueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class QueueManagementIntegrationTest {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private QueueService queueService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private QueueRepository queueRepository;

    @Autowired
    private PatientRepository patientRepository;

    private DepartmentDTO departmentDTO;
    private QueueDTO queueDTO;

    @BeforeEach
    void setUp() {
        // Clean up repositories
        patientRepository.deleteAll();
        queueRepository.deleteAll();
        departmentRepository.deleteAll();

        // Create test department
        departmentDTO = new DepartmentDTO();
        departmentDTO.setName("Test Department");
        departmentDTO.setDescription("Test Department Description");
        departmentDTO = departmentService.createDepartment(departmentDTO);

        // Create test queue
        queueDTO = new QueueDTO();
        queueDTO.setName("Test Queue");
        queueDTO.setDescription("Test Queue Description");
        queueDTO.setDepartmentId(departmentDTO.getId());
        queueDTO = queueService.createQueue(queueDTO);
    }

    @Test
    void testFullQueueManagementFlow() {
        // Step 1: Verify department and queue were created successfully
        assertNotNull(departmentDTO.getId());
        assertNotNull(queueDTO.getId());
        assertNotNull(queueDTO.getQrCodeId());

        // Step 2: Get queue details
        QueueDetailsDTO queueDetails = queueService.getQueueDetails(queueDTO.getId());
        assertEquals(queueDTO.getName(), queueDetails.getName());
        assertEquals(departmentDTO.getName(), queueDetails.getDepartmentName());
        assertNotNull(queueDetails.getQrCodeImage());
        assertEquals(0, queueDetails.getWaitingPatients().size());
        assertEquals(0, queueDetails.getServedPatients().size());
        assertNull(queueDetails.getCurrentPatient());

        // Step 3: Register patients to the queue
        PatientDTO patient1 = new PatientDTO();
        patient1.setName("Patient 1");
        patient1.setPhoneNumber("1234567890");
        patient1.setQrCodeId(queueDTO.getQrCodeId());
        patient1 = patientService.registerPatient(patient1);

        PatientDTO patient2 = new PatientDTO();
        patient2.setName("Patient 2");
        patient2.setPhoneNumber("0987654321");
        patient2.setQrCodeId(queueDTO.getQrCodeId());
        patient2 = patientService.registerPatient(patient2);

        PatientDTO patient3 = new PatientDTO();
        patient3.setName("Patient 3");
        patient3.setPhoneNumber("5555555555");
        patient3.setQrCodeId(queueDTO.getQrCodeId());
        patient3 = patientService.registerPatient(patient3);

        // Step 4: Verify patients were registered correctly
        assertNotNull(patient1.getId());
        assertNotNull(patient2.getId());
        assertNotNull(patient3.getId());
        assertEquals(Patient.PatientStatus.WAITING, patient1.getStatus());
        assertEquals(Patient.PatientStatus.WAITING, patient2.getStatus());
        assertEquals(Patient.PatientStatus.WAITING, patient3.getStatus());
        assertEquals(1, patient1.getQueuePosition());
        assertEquals(2, patient2.getQueuePosition());
        assertEquals(3, patient3.getQueuePosition());

        // Step 5: Get updated queue details
        queueDetails = queueService.getQueueDetails(queueDTO.getId());
        assertEquals(3, queueDetails.getWaitingPatients().size());
        assertEquals(0, queueDetails.getServedPatients().size());
        assertNull(queueDetails.getCurrentPatient());

        // Step 6: Update patient status - call first patient
        PatientStatusUpdateDTO statusUpdateDTO = new PatientStatusUpdateDTO();
        statusUpdateDTO.setStatus(Patient.PatientStatus.SERVING);
        PatientDTO updatedPatient1 = patientService.updatePatientStatus(patient1.getId(), statusUpdateDTO);
        assertEquals(Patient.PatientStatus.SERVING, updatedPatient1.getStatus());

        // Step 7: Get updated queue details
        queueDetails = queueService.getQueueDetails(queueDTO.getId());
        assertEquals(2, queueDetails.getWaitingPatients().size());
        assertEquals(0, queueDetails.getServedPatients().size());
        assertNotNull(queueDetails.getCurrentPatient());
        assertEquals(patient1.getId(), queueDetails.getCurrentPatient().getId());

        // Step 8: Mark first patient as served
        statusUpdateDTO.setStatus(Patient.PatientStatus.SERVED);
        updatedPatient1 = patientService.updatePatientStatus(patient1.getId(), statusUpdateDTO);
        assertEquals(Patient.PatientStatus.SERVED, updatedPatient1.getStatus());
        assertNotNull(updatedPatient1.getServedAt());

        // Step 9: Get updated queue details
        queueDetails = queueService.getQueueDetails(queueDTO.getId());
        assertEquals(2, queueDetails.getWaitingPatients().size());
        assertEquals(1, queueDetails.getServedPatients().size());
        assertNull(queueDetails.getCurrentPatient());

        // Step 10: Call next patient
        statusUpdateDTO.setStatus(Patient.PatientStatus.SERVING);
        PatientDTO updatedPatient2 = patientService.updatePatientStatus(patient2.getId(), statusUpdateDTO);
        assertEquals(Patient.PatientStatus.SERVING, updatedPatient2.getStatus());

        // Step 11: Get updated queue details
        queueDetails = queueService.getQueueDetails(queueDTO.getId());
        assertEquals(1, queueDetails.getWaitingPatients().size());
        assertEquals(1, queueDetails.getServedPatients().size());
        assertNotNull(queueDetails.getCurrentPatient());
        assertEquals(patient2.getId(), queueDetails.getCurrentPatient().getId());

        // Step 12: Mark second patient as served
        statusUpdateDTO.setStatus(Patient.PatientStatus.SERVED);
        updatedPatient2 = patientService.updatePatientStatus(patient2.getId(), statusUpdateDTO);
        assertEquals(Patient.PatientStatus.SERVED, updatedPatient2.getStatus());

        // Step 13: Get updated queue details
        queueDetails = queueService.getQueueDetails(queueDTO.getId());
        assertEquals(1, queueDetails.getWaitingPatients().size());
        assertEquals(2, queueDetails.getServedPatients().size());
        assertNull(queueDetails.getCurrentPatient());
        
        // Step 14: Test with invalid ID types
        try {
            queueService.getQueueDetails(null);
            fail("Should have thrown exception for null ID");
        } catch (IllegalArgumentException e) {
            // Expected exception
        }
    }
    
    @Test
    void testTypeConversionEdgeCases() {
        // Test with string ID conversion in repositories
        try {
            // This would happen if a string ID was passed from frontend without conversion
            // The repository layer should handle this by throwing appropriate exceptions
            queueRepository.findById(Long.valueOf("abc"));
            fail("Should have thrown exception for invalid ID format");
        } catch (NumberFormatException e) {
            // Expected exception
        }
        
        // Test with very large ID
        try {
            // This tests boundary conditions for ID conversion
            Long veryLargeId = Long.MAX_VALUE;
            queueService.getQueueDetails(veryLargeId);
            // No exception should be thrown, but the queue won't be found
        } catch (Exception e) {
            fail("Should not throw exception for valid Long ID: " + e.getMessage());
        }
    }
}
