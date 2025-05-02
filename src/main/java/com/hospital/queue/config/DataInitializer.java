package com.hospital.queue.config;

import com.hospital.queue.model.Department;
import com.hospital.queue.model.Hospital;
import com.hospital.queue.model.Queue;
import com.hospital.queue.repository.DepartmentRepository;
import com.hospital.queue.repository.HospitalRepository;
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
            HospitalRepository hospitalRepository,
            DepartmentRepository departmentRepository,
            QueueRepository queueRepository,
            QRCodeService qrCodeService) {
        
        return args -> {
            System.out.println("Initializing test data...");
            
            // Check if we already have data
            if (hospitalRepository.count() > 0) {
                System.out.println("Database already contains data, skipping initialization");
                return;
            }
            
            // Create test hospitals
            Hospital cityHospital = new Hospital();
            cityHospital.setName("City General Hospital");
            cityHospital.setAddress("123 Main Street, City Center");
            cityHospital.setContactNumber("+1-555-123-4567");
            cityHospital.setEmail("info@cityhospital.com");
            cityHospital.setDescription("A leading general hospital serving the city center area");
            cityHospital = hospitalRepository.save(cityHospital);
            
            Hospital memorialHospital = new Hospital();
            memorialHospital.setName("Memorial Medical Center");
            memorialHospital.setAddress("456 Park Avenue, Westside");
            memorialHospital.setContactNumber("+1-555-987-6543");
            memorialHospital.setEmail("contact@memorialmedical.com");
            memorialHospital.setDescription("Specialized medical center with advanced treatment facilities");
            memorialHospital = hospitalRepository.save(memorialHospital);
            
            // Create test departments for City Hospital
            Department emergency = new Department();
            emergency.setName("Emergency");
            emergency.setDescription("Emergency department for urgent care");
            emergency.setHospital(cityHospital);
            emergency = departmentRepository.save(emergency);
            
            Department cardiology = new Department();
            cardiology.setName("Cardiology");
            cardiology.setDescription("Heart and cardiovascular system");
            cardiology.setHospital(cityHospital);
            cardiology = departmentRepository.save(cardiology);
            
            // Create test departments for Memorial Hospital
            Department pediatrics = new Department();
            pediatrics.setName("Pediatrics");
            pediatrics.setDescription("Children's health services");
            pediatrics.setHospital(memorialHospital);
            pediatrics = departmentRepository.save(pediatrics);
            
            Department orthopedics = new Department();
            orthopedics.setName("Orthopedics");
            orthopedics.setDescription("Bone and joint care");
            orthopedics.setHospital(memorialHospital);
            orthopedics = departmentRepository.save(orthopedics);
            
            // Create real queues for City Hospital departments
            createQueue(queueRepository, qrCodeService, emergency, "Emergency Triage", "Initial assessment for emergency patients");
            createQueue(queueRepository, qrCodeService, emergency, "Emergency Treatment", "Treatment for emergency patients");
            
            createQueue(queueRepository, qrCodeService, cardiology, "Cardiology Consultation", "Consultation with cardiologists");
            createQueue(queueRepository, qrCodeService, cardiology, "Cardiac Testing", "ECG and other cardiac tests");
            
            // Create real queues for Memorial Hospital departments
            createQueue(queueRepository, qrCodeService, pediatrics, "Pediatric General", "General pediatric consultations");
            createQueue(queueRepository, qrCodeService, pediatrics, "Pediatric Vaccination", "Childhood vaccinations");
            
            createQueue(queueRepository, qrCodeService, orthopedics, "Fracture Clinic", "Treatment for bone fractures");
            createQueue(queueRepository, qrCodeService, orthopedics, "Joint Replacement", "Consultation for joint replacement surgeries");
            
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
