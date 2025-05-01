package com.hospital.queue.service;

import com.hospital.queue.dto.QueueDTO;
import com.hospital.queue.dto.QueueDetailsDTO;
import com.hospital.queue.exception.ResourceNotFoundException;
import com.hospital.queue.model.Department;
import com.hospital.queue.model.Patient;
import com.hospital.queue.model.Queue;
import com.hospital.queue.repository.DepartmentRepository;
import com.hospital.queue.repository.PatientRepository;
import com.hospital.queue.repository.QueueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QueueServiceTest {

    @Mock
    private QueueRepository queueRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private QRCodeService qrCodeService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private QueueService queueService;

    private Department department;
    private Queue queue;
    private QueueDTO queueDTO;
    private List<Patient> patients;

    @BeforeEach
    void setUp() {
        // Setup department
        department = new Department();
        department.setId(1L);
        department.setName("Cardiology");
        department.setDescription("Department for heart-related issues");

        // Setup queue
        queue = new Queue();
        queue.setId(1L);
        queue.setName("Cardiology Queue");
        queue.setDescription("Queue for cardiology department");
        queue.setDepartment(department);
        queue.setQrCodeId("abc123");
        queue.setCreatedAt(LocalDateTime.now());

        // Setup queue DTO
        queueDTO = new QueueDTO();
        queueDTO.setName("Cardiology Queue");
        queueDTO.setDescription("Queue for cardiology department");
        queueDTO.setDepartmentId(1L);

        // Setup patients
        Patient patient1 = new Patient();
        patient1.setId(1L);
        patient1.setName("John Doe");
        patient1.setPhoneNumber("1234567890");
        patient1.setQueue(queue);
        patient1.setStatus(Patient.PatientStatus.WAITING);
        patient1.setQueuePosition(1);
        patient1.setJoinedAt(LocalDateTime.now());

        Patient patient2 = new Patient();
        patient2.setId(2L);
        patient2.setName("Jane Smith");
        patient2.setPhoneNumber("0987654321");
        patient2.setQueue(queue);
        patient2.setStatus(Patient.PatientStatus.SERVING);
        patient2.setQueuePosition(2);
        patient2.setJoinedAt(LocalDateTime.now());

        patients = Arrays.asList(patient1, patient2);
    }

    @Test
    void getAllQueues_ShouldReturnAllQueues() {
        // Arrange
        List<Queue> queues = Arrays.asList(queue);
        when(queueRepository.findAll()).thenReturn(queues);

        // Act
        List<QueueDTO> result = queueService.getAllQueues();

        // Assert
        assertEquals(1, result.size());
        assertEquals("Cardiology Queue", result.get(0).getName());
        assertEquals(1L, result.get(0).getDepartmentId());
    }

    @Test
    void getQueuesByDepartment_ShouldReturnQueuesForDepartment() {
        // Arrange
        List<Queue> queues = Arrays.asList(queue);
        when(queueRepository.findByDepartmentId(1L)).thenReturn(queues);

        // Act
        List<QueueDTO> result = queueService.getQueuesByDepartment(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Cardiology Queue", result.get(0).getName());
        assertEquals(1L, result.get(0).getDepartmentId());
    }

    @Test
    void getQueueDetails_ShouldReturnQueueDetails() throws Exception {
        // Arrange
        when(queueRepository.findById(1L)).thenReturn(Optional.of(queue));
        when(qrCodeService.generateQRCodeImage(anyString(), anyInt(), anyInt())).thenReturn("base64-encoded-image");
        
        List<Patient> servingPatients = new ArrayList<>();
        Patient servingPatient = new Patient();
        servingPatient.setId(2L);
        servingPatient.setStatus(Patient.PatientStatus.SERVING);
        servingPatients.add(servingPatient);
        
        List<Patient> waitingPatients = new ArrayList<>();
        Patient waitingPatient = new Patient();
        waitingPatient.setId(1L);
        waitingPatient.setStatus(Patient.PatientStatus.WAITING);
        waitingPatients.add(waitingPatient);
        
        List<Patient> servedPatients = new ArrayList<>();
        
        when(patientRepository.findByQueueIdAndStatusOrderByQueuePosition(1L, Patient.PatientStatus.SERVING))
                .thenReturn(servingPatients);
        when(patientRepository.findByQueueIdAndStatusOrderByQueuePosition(1L, Patient.PatientStatus.WAITING))
                .thenReturn(waitingPatients);
        when(patientRepository.findByQueueIdAndStatusInOrderByQueuePosition(eq(1L), anyList()))
                .thenReturn(servedPatients);

        // Act
        QueueDetailsDTO result = queueService.getQueueDetails(1L);

        // Assert
        assertEquals(1L, result.getId());
        assertEquals("Cardiology Queue", result.getName());
        assertEquals("Cardiology", result.getDepartmentName());
        assertEquals("abc123", result.getQrCodeId());
        assertEquals("base64-encoded-image", result.getQrCodeImage());
        assertNotNull(result.getCurrentPatient());
        assertEquals(1, result.getWaitingPatients().size());
        assertEquals(0, result.getServedPatients().size());
    }

    @Test
    void getQueueDetails_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(queueRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            queueService.getQueueDetails(999L);
        });
    }

    @Test
    void createQueue_ShouldCreateAndReturnQueue() {
        // Arrange
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(qrCodeService.generateQRCodeId()).thenReturn("new-qr-code-id");
        when(queueRepository.save(any(Queue.class))).thenAnswer(invocation -> {
            Queue savedQueue = invocation.getArgument(0);
            savedQueue.setId(1L);
            return savedQueue;
        });

        // Act
        QueueDTO result = queueService.createQueue(queueDTO);

        // Assert
        assertEquals("Cardiology Queue", result.getName());
        assertEquals(1L, result.getDepartmentId());
        assertEquals("new-qr-code-id", result.getQrCodeId());
        verify(notificationService).broadcastQueueUpdate(anyLong());
    }

    @Test
    void createQueue_WithInvalidDepartmentId_ShouldThrowException() {
        // Arrange
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());
        queueDTO.setDepartmentId(999L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            queueService.createQueue(queueDTO);
        });
    }

    @Test
    void updateQueue_ShouldUpdateAndReturnQueue() {
        // Arrange
        when(queueRepository.findById(1L)).thenReturn(Optional.of(queue));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(queueRepository.save(any(Queue.class))).thenReturn(queue);

        QueueDTO updateDTO = new QueueDTO();
        updateDTO.setName("Updated Queue");
        updateDTO.setDescription("Updated description");
        updateDTO.setDepartmentId(1L);

        // Act
        QueueDTO result = queueService.updateQueue(1L, updateDTO);

        // Assert
        assertEquals("Updated Queue", queue.getName());
        assertEquals("Updated description", queue.getDescription());
        assertEquals(1L, result.getDepartmentId());
    }

    @Test
    void updateQueue_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(queueRepository.findById(999L)).thenReturn(Optional.empty());

        QueueDTO updateDTO = new QueueDTO();
        updateDTO.setName("Updated Queue");
        updateDTO.setDepartmentId(1L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            queueService.updateQueue(999L, updateDTO);
        });
    }

    @Test
    void deleteQueue_ShouldDeleteQueue() {
        // Arrange
        when(queueRepository.existsById(1L)).thenReturn(true);

        // Act
        queueService.deleteQueue(1L);

        // Assert
        verify(queueRepository).deleteById(1L);
    }

    @Test
    void deleteQueue_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(queueRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            queueService.deleteQueue(999L);
        });
    }
    
    @Test
    void getQueueDetails_WithNullId_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            queueService.getQueueDetails(null);
        });
    }
}
