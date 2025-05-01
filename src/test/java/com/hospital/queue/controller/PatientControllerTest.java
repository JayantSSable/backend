package com.hospital.queue.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.queue.dto.PatientDTO;
import com.hospital.queue.dto.PatientStatusUpdateDTO;
import com.hospital.queue.model.Patient;
import com.hospital.queue.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientController.class)
public class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientService patientService;

    @Autowired
    private ObjectMapper objectMapper;

    private PatientDTO patientDTO;
    private List<PatientDTO> patientDTOList;

    @BeforeEach
    void setUp() {
        // Setup test data
        patientDTO = new PatientDTO();
        patientDTO.setId(1L);
        patientDTO.setName("John Doe");
        patientDTO.setPhoneNumber("1234567890");
        patientDTO.setEmail("john.doe@example.com");
        patientDTO.setQueueId(1L);
        patientDTO.setQrCodeId("abc123");
        patientDTO.setStatus(Patient.PatientStatus.WAITING);
        patientDTO.setQueuePosition(1);
        patientDTO.setJoinedAt(LocalDateTime.now());

        PatientDTO patientDTO2 = new PatientDTO();
        patientDTO2.setId(2L);
        patientDTO2.setName("Jane Smith");
        patientDTO2.setPhoneNumber("0987654321");
        patientDTO2.setEmail("jane.smith@example.com");
        patientDTO2.setQueueId(1L);
        patientDTO2.setQrCodeId("abc123");
        patientDTO2.setStatus(Patient.PatientStatus.WAITING);
        patientDTO2.setQueuePosition(2);
        patientDTO2.setJoinedAt(LocalDateTime.now());

        patientDTOList = Arrays.asList(patientDTO, patientDTO2);
    }

    @Test
    void getAllPatients_ShouldReturnListOfPatients() throws Exception {
        when(patientService.getAllPatients()).thenReturn(patientDTOList);

        mockMvc.perform(get("/api/patients")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("John Doe")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Jane Smith")));
    }

    @Test
    void getPatientById_ShouldReturnPatient() throws Exception {
        when(patientService.getPatientById(1L)).thenReturn(patientDTO);

        mockMvc.perform(get("/api/patients/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.phoneNumber", is("1234567890")))
                .andExpect(jsonPath("$.queueId", is(1)));
    }

    @Test
    void registerPatient_ShouldReturnRegisteredPatient() throws Exception {
        PatientDTO inputDTO = new PatientDTO();
        inputDTO.setName("New Patient");
        inputDTO.setPhoneNumber("5555555555");
        inputDTO.setEmail("new.patient@example.com");
        inputDTO.setQrCodeId("abc123");

        when(patientService.registerPatient(any(PatientDTO.class))).thenReturn(patientDTO);

        mockMvc.perform(post("/api/patients/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")));
    }

    @Test
    void updatePatientStatus_ShouldReturnUpdatedPatient() throws Exception {
        PatientStatusUpdateDTO statusUpdateDTO = new PatientStatusUpdateDTO();
        statusUpdateDTO.setStatus(Patient.PatientStatus.SERVING);

        PatientDTO updatedDTO = new PatientDTO();
        updatedDTO.setId(1L);
        updatedDTO.setName("John Doe");
        updatedDTO.setPhoneNumber("1234567890");
        updatedDTO.setEmail("john.doe@example.com");
        updatedDTO.setQueueId(1L);
        updatedDTO.setQrCodeId("abc123");
        updatedDTO.setStatus(Patient.PatientStatus.SERVING);
        updatedDTO.setQueuePosition(1);
        updatedDTO.setJoinedAt(LocalDateTime.now());

        when(patientService.updatePatientStatus(anyLong(), any(PatientStatusUpdateDTO.class))).thenReturn(updatedDTO);

        mockMvc.perform(patch("/api/patients/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.status", is("SERVING")));
    }
    
    @Test
    void getPatientById_WithStringId_ShouldHandleTypeConversion() throws Exception {
        // This test verifies that string IDs are properly converted to Long
        when(patientService.getPatientById(1L)).thenReturn(patientDTO);

        mockMvc.perform(get("/api/patients/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }
    
    @Test
    void updatePatientStatus_WithStringId_ShouldHandleTypeConversion() throws Exception {
        // This test verifies that string IDs in URL paths are properly converted to Long
        PatientStatusUpdateDTO statusUpdateDTO = new PatientStatusUpdateDTO();
        statusUpdateDTO.setStatus(Patient.PatientStatus.SERVING);

        PatientDTO updatedDTO = new PatientDTO();
        updatedDTO.setId(1L);
        updatedDTO.setStatus(Patient.PatientStatus.SERVING);

        when(patientService.updatePatientStatus(1L, statusUpdateDTO)).thenReturn(updatedDTO);

        mockMvc.perform(patch("/api/patients/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("SERVING")));
    }
}
