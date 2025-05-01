package com.hospital.queue.controller;

import com.hospital.queue.dto.QueueDTO;
import com.hospital.queue.dto.QueueDetailsDTO;
import com.hospital.queue.service.QueueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/queues")
@CrossOrigin(origins = "*")
public class QueueController {

    private final QueueService queueService;
    
    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    @GetMapping
    public ResponseEntity<List<QueueDTO>> getAllQueues() {
        return ResponseEntity.ok(queueService.getAllQueues());
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<QueueDTO>> getQueuesByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(queueService.getQueuesByDepartment(departmentId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QueueDetailsDTO> getQueueDetails(@PathVariable Long id) {
        return ResponseEntity.ok(queueService.getQueueDetails(id));
    }

    @PostMapping
    public ResponseEntity<QueueDTO> createQueue(@Valid @RequestBody QueueDTO queueDTO) {
        QueueDTO createdQueue = queueService.createQueue(queueDTO);
        return new ResponseEntity<>(createdQueue, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QueueDTO> updateQueue(
            @PathVariable Long id,
            @Valid @RequestBody QueueDTO queueDTO) {
        return ResponseEntity.ok(queueService.updateQueue(id, queueDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQueue(@PathVariable Long id) {
        queueService.deleteQueue(id);
        return ResponseEntity.noContent().build();
    }
}
