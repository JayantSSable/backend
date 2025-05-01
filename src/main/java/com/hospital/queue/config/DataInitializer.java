package com.hospital.queue.config;

import com.hospital.queue.model.Department;
import com.hospital.queue.model.Queue;
import com.hospital.queue.repository.DepartmentRepository;
import com.hospital.queue.repository.QueueRepository;
import com.hospital.queue.service.QRCodeService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    /**
     * Initialize test data for development environment
     */
    @Bean
    public CommandLineRunner initData(
            DepartmentRepository departmentRepository,
            QueueRepository queueRepository,
            QRCodeService qrCodeService) {
        
        return args -> {
            System.out.println("Initializing test data...");
            
            // Check if we already have data
            if (departmentRepository.count() > 0) {
                System.out.println("Database already contains data, skipping initialization");
                return;
            }
            
            // Create test departments
            Department emergency = new Department();
            emergency.setName("Emergency");
            emergency.setDescription("Emergency department for urgent care");
            emergency = departmentRepository.save(emergency);
            
            Department cardiology = new Department();
            cardiology.setName("Cardiology");
            cardiology.setDescription("Heart and cardiovascular system");
            cardiology = departmentRepository.save(cardiology);
            
            Department pediatrics = new Department();
            pediatrics.setName("Pediatrics");
            pediatrics.setDescription("Children's health services");
            pediatrics = departmentRepository.save(pediatrics);
            
            // Create real queues for each department
            createQueue(queueRepository, qrCodeService, emergency, "Emergency Triage", "Initial assessment for emergency patients");
            createQueue(queueRepository, qrCodeService, emergency, "Emergency Treatment", "Treatment for emergency patients");
            
            createQueue(queueRepository, qrCodeService, cardiology, "Cardiology Consultation", "Consultation with cardiologists");
            createQueue(queueRepository, qrCodeService, cardiology, "Cardiac Testing", "ECG and other cardiac tests");
            
            createQueue(queueRepository, qrCodeService, pediatrics, "Pediatric General", "General pediatric consultations");
            createQueue(queueRepository, qrCodeService, pediatrics, "Pediatric Vaccination", "Childhood vaccinations");
            
            System.out.println("Test data initialization complete!");
        };
    }
    
    private Queue createQueue(QueueRepository queueRepository, QRCodeService qrCodeService, 
                             Department department, String name, String description) {
        Queue queue = new Queue();
        queue.setName(name);
        queue.setDescription(description);
        queue.setDepartment(department);
        queue.setQrCodeId(qrCodeService.generateQRCodeId());
        return queueRepository.save(queue);
    }
}
