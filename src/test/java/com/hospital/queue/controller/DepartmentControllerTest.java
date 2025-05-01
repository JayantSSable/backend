package com.hospital.queue.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.queue.dto.DepartmentDTO;
import com.hospital.queue.service.DepartmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(DepartmentController.class)
public class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepartmentService departmentService;

    @Autowired
    private ObjectMapper objectMapper;

    private DepartmentDTO departmentDTO;
    private List<DepartmentDTO> departmentDTOList;

    @BeforeEach
    void setUp() {
        // Setup test data
        departmentDTO = new DepartmentDTO();
        departmentDTO.setId(1L);
        departmentDTO.setName("Cardiology");
        departmentDTO.setDescription("Department for heart-related issues");

        DepartmentDTO departmentDTO2 = new DepartmentDTO();
        departmentDTO2.setId(2L);
        departmentDTO2.setName("Neurology");
        departmentDTO2.setDescription("Department for brain and nervous system");

        departmentDTOList = Arrays.asList(departmentDTO, departmentDTO2);
    }

    @Test
    void getAllDepartments_ShouldReturnListOfDepartments() throws Exception {
        when(departmentService.getAllDepartments()).thenReturn(departmentDTOList);

        mockMvc.perform(get("/api/departments")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Cardiology")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Neurology")));
    }

    @Test
    void getDepartmentById_ShouldReturnDepartment() throws Exception {
        when(departmentService.getDepartmentById(1L)).thenReturn(departmentDTO);

        mockMvc.perform(get("/api/departments/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Cardiology")))
                .andExpect(jsonPath("$.description", is("Department for heart-related issues")));
    }

    @Test
    void createDepartment_ShouldReturnCreatedDepartment() throws Exception {
        DepartmentDTO inputDTO = new DepartmentDTO();
        inputDTO.setName("New Department");
        inputDTO.setDescription("New department description");

        when(departmentService.createDepartment(any(DepartmentDTO.class))).thenReturn(departmentDTO);

        mockMvc.perform(post("/api/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Cardiology")));
    }

    @Test
    void updateDepartment_ShouldReturnUpdatedDepartment() throws Exception {
        DepartmentDTO inputDTO = new DepartmentDTO();
        inputDTO.setName("Updated Department");
        inputDTO.setDescription("Updated description");

        DepartmentDTO updatedDTO = new DepartmentDTO();
        updatedDTO.setId(1L);
        updatedDTO.setName("Updated Department");
        updatedDTO.setDescription("Updated description");

        when(departmentService.updateDepartment(anyLong(), any(DepartmentDTO.class))).thenReturn(updatedDTO);

        mockMvc.perform(put("/api/departments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Updated Department")))
                .andExpect(jsonPath("$.description", is("Updated description")));
    }

    @Test
    void deleteDepartment_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/departments/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
    
    @Test
    void getDepartmentById_WithStringId_ShouldHandleTypeConversion() throws Exception {
        // This test verifies that string IDs are properly converted to Long
        when(departmentService.getDepartmentById(1L)).thenReturn(departmentDTO);

        mockMvc.perform(get("/api/departments/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }
}
