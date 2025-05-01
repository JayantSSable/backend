package com.hospital.queue.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.queue.dto.QueueDTO;
import com.hospital.queue.dto.QueueDetailsDTO;
import com.hospital.queue.model.Department;
import com.hospital.queue.model.Patient;
import com.hospital.queue.model.Queue;
import com.hospital.queue.service.QueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QueueController.class)
public class QueueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QueueService queueService;

    @Autowired
    private ObjectMapper objectMapper;

    private QueueDTO queueDTO;
    private QueueDetailsDTO queueDetailsDTO;
    private List<QueueDTO> queueDTOList;

    @BeforeEach
    void setUp() {
        // Setup test data
        queueDTO = new QueueDTO();
        queueDTO.setId(1L);
        queueDTO.setName("Cardiology Queue");
        queueDTO.setDescription("Queue for cardiology department");
        queueDTO.setDepartmentId(1L);
        queueDTO.setQrCodeId("abc123");

        QueueDTO queueDTO2 = new QueueDTO();
        queueDTO2.setId(2L);
        queueDTO2.setName("Neurology Queue");
        queueDTO2.setDescription("Queue for neurology department");
        queueDTO2.setDepartmentId(2L);
        queueDTO2.setQrCodeId("def456");

        queueDTOList = Arrays.asList(queueDTO, queueDTO2);

        // Setup queue details DTO
        queueDetailsDTO = new QueueDetailsDTO();
        queueDetailsDTO.setId(1L);
        queueDetailsDTO.setName("Cardiology Queue");
        queueDetailsDTO.setDescription("Queue for cardiology department");
        queueDetailsDTO.setDepartmentId(1L);
        queueDetailsDTO.setDepartmentName("Cardiology");
        queueDetailsDTO.setQrCodeId("abc123");
        queueDetailsDTO.setQrCodeImage("base64-encoded-image");
        
        // Setup current patient
        Patient currentPatient = new Patient();
        currentPatient.setId(1L);
        currentPatient.setName("John Doe");
        currentPatient.setStatus(Patient.PatientStatus.SERVING);
        currentPatient.setQueuePosition(1);
        currentPatient.setJoinedAt(LocalDateTime.now().minusMinutes(30));
        
        // Setup waiting patients
        Patient waitingPatient = new Patient();
        waitingPatient.setId(2L);
        waitingPatient.setName("Jane Smith");
        waitingPatient.setStatus(Patient.PatientStatus.WAITING);
        waitingPatient.setQueuePosition(2);
        waitingPatient.setJoinedAt(LocalDateTime.now().minusMinutes(20));
        
        // Setup served patients
        Patient servedPatient = new Patient();
        servedPatient.setId(3L);
        servedPatient.setName("Bob Johnson");
        servedPatient.setStatus(Patient.PatientStatus.SERVED);
        servedPatient.setQueuePosition(0);
        servedPatient.setJoinedAt(LocalDateTime.now().minusMinutes(60));
        servedPatient.setServedAt(LocalDateTime.now().minusMinutes(40));
        
        queueDetailsDTO.setCurrentPatient(currentPatient);
        queueDetailsDTO.setWaitingPatients(Collections.singletonList(waitingPatient));
        queueDetailsDTO.setServedPatients(Collections.singletonList(servedPatient));
        queueDetailsDTO.setWaitingCount(1);
        queueDetailsDTO.setServedCount(1);
    }

    @Test
    void getAllQueues_ShouldReturnListOfQueues() throws Exception {
        when(queueService.getAllQueues()).thenReturn(queueDTOList);

        mockMvc.perform(get("/api/queues")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Cardiology Queue")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Neurology Queue")));
    }

    @Test
    void getQueuesByDepartment_ShouldReturnQueuesForDepartment() throws Exception {
        when(queueService.getQueuesByDepartment(1L)).thenReturn(Collections.singletonList(queueDTO));

        mockMvc.perform(get("/api/queues/department/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Cardiology Queue")))
                .andExpect(jsonPath("$[0].departmentId", is(1)));
    }

    @Test
    void getQueueDetails_ShouldReturnQueueDetails() throws Exception {
        when(queueService.getQueueDetails(1L)).thenReturn(queueDetailsDTO);

        mockMvc.perform(get("/api/queues/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Cardiology Queue")))
                .andExpect(jsonPath("$.departmentId", is(1)))
                .andExpect(jsonPath("$.departmentName", is("Cardiology")))
                .andExpect(jsonPath("$.currentPatient.name", is("John Doe")))
                .andExpect(jsonPath("$.waitingPatients", hasSize(1)))
                .andExpect(jsonPath("$.servedPatients", hasSize(1)));
    }

    @Test
    void createQueue_ShouldReturnCreatedQueue() throws Exception {
        QueueDTO inputDTO = new QueueDTO();
        inputDTO.setName("New Queue");
        inputDTO.setDescription("New queue description");
        inputDTO.setDepartmentId(1L);

        when(queueService.createQueue(any(QueueDTO.class))).thenReturn(queueDTO);

        mockMvc.perform(post("/api/queues")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Cardiology Queue")));
    }

    @Test
    void updateQueue_ShouldReturnUpdatedQueue() throws Exception {
        QueueDTO inputDTO = new QueueDTO();
        inputDTO.setName("Updated Queue");
        inputDTO.setDescription("Updated description");
        inputDTO.setDepartmentId(1L);

        QueueDTO updatedDTO = new QueueDTO();
        updatedDTO.setId(1L);
        updatedDTO.setName("Updated Queue");
        updatedDTO.setDescription("Updated description");
        updatedDTO.setDepartmentId(1L);
        updatedDTO.setQrCodeId("abc123");

        when(queueService.updateQueue(anyLong(), any(QueueDTO.class))).thenReturn(updatedDTO);

        mockMvc.perform(put("/api/queues/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Updated Queue")))
                .andExpect(jsonPath("$.description", is("Updated description")));
    }

    @Test
    void deleteQueue_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/queues/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
    
    @Test
    void getQueueDetails_WithStringId_ShouldHandleTypeConversion() throws Exception {
        // This test verifies that string IDs are properly converted to Long
        when(queueService.getQueueDetails(1L)).thenReturn(queueDetailsDTO);

        mockMvc.perform(get("/api/queues/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }
}
