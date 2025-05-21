package phong.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import phong.demo.Controller.Service_Controller;
import phong.demo.DTO.JSOn_objectString;
import phong.demo.DTO.Service_DTO;
import phong.demo.Entity.Service;
import phong.demo.Repository.ServiceRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceControllerTest {

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private Service_Controller controller;

    @Captor
    private ArgumentCaptor<Service> serviceCaptor;

    private Service svc1, svc2;

    @BeforeEach
    void setUp() {
        svc1 = new Service();
        svc1.setId(1L);
        svc1.setService_name("Manicure");
        svc1.setPrice(20);
        svc1.setDuration(30);

        svc2 = new Service();
        svc2.setId(2L);
        svc2.setService_name("Pedicure");
        svc2.setPrice(25);
        svc2.setDuration(45);
    }

    // --- getAllServices ---
//    @Test
//    void testGetAllServices_returnsDtoList() {
//        when(serviceRepository.findAll()).thenReturn(Arrays.asList(svc1, svc2));
//
//        JSon_Object<List<Service_DTO>> result = controller.getAllServices();
//        List<Service_DTO> dtos = result.get;
//
//        assertThat(dtos).hasSize(2);
//        assertThat(dtos)
//                .extracting(Service_DTO::getService_name)
//                .containsExactly("Manicure", "Pedicure");
//    }

    // --- getServiceById ---
    @Test
    void testGetServiceById_found() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(svc1));

        ResponseEntity<?> resp = controller.getServiceById(1L);
        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
        assertThat(resp.getBody()).isSameAs(svc1);
    }

    @Test
    void testGetServiceById_notFound() {
        when(serviceRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<?> resp = controller.getServiceById(99L);
        assertThat(resp.getStatusCodeValue()).isEqualTo(404);
    }

    // --- createService ---
    @Test
    void testCreateService_success() {
        Service_DTO dto = new Service_DTO("Gel", 30, 0);
        when(serviceRepository.existsByServiceName("Gel")).thenReturn(false);
        when(serviceRepository.save(any())).thenAnswer(inv -> {
            Service s = inv.getArgument(0);
            s.setId(5L);
            return s;
        });

        ResponseEntity<?> resp = controller.createService(dto);
        assertThat(resp.getStatusCodeValue()).isEqualTo(200);

        // verify saved entity
        verify(serviceRepository).save(serviceCaptor.capture());
        Service saved = serviceCaptor.getValue();
        assertThat(saved.getService_name()).isEqualTo("Gel");
        assertThat(saved.getPrice()).isEqualTo(30);
    }

    @Test
    void testCreateService_invalidData() {
        Service_DTO dto1 = new Service_DTO("", 10, 0);
        Service_DTO dto2 = new Service_DTO("X", -5, 0);

        ResponseEntity<?> r1 = controller.createService(dto1);
        ResponseEntity<?> r2 = controller.createService(dto2);

        assertThat(r1.getStatusCodeValue()).isEqualTo(400);
        assertThat(r2.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    void testCreateService_alreadyExists() {
        Service_DTO dto = new Service_DTO("Manicure", 20, 0);
        when(serviceRepository.existsByServiceName("Manicure")).thenReturn(true);

        ResponseEntity<?> resp = controller.createService(dto);
        assertThat(resp.getStatusCodeValue()).isEqualTo(400);
        assertThat(resp.getBody()).isInstanceOf(JSOn_objectString.class);
    }

    // --- updateService ---
    @Test
    void testUpdateService_success() {
        Service_DTO dto = new Service_DTO("Deluxe Manicure", 35, 0);
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(svc1));
        when(serviceRepository.existsByServiceName("Deluxe Manicure")).thenReturn(false);
        when(serviceRepository.save(any())).thenReturn(svc1);

        ResponseEntity<?> resp = controller.updateService(1L, dto);
        assertThat(resp.getStatusCodeValue()).isEqualTo(200);

        verify(serviceRepository).save(serviceCaptor.capture());
        Service updated = serviceCaptor.getValue();
        assertThat(updated.getService_name()).isEqualTo("Deluxe Manicure");
        assertThat(updated.getPrice()).isEqualTo(35);
    }

    @Test
    void testUpdateService_notFound() {
        when(serviceRepository.findById(9L)).thenReturn(Optional.empty());

        ResponseEntity<?> resp = controller.updateService(9L, new Service_DTO("A", 10, 0));
        assertThat(resp.getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    void testUpdateService_nameConflictOrNull() {
        Service_DTO dto = new Service_DTO(null, 20, 0);
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(svc1));

        ResponseEntity<?> resp = controller.updateService(1L, dto);
        assertThat(resp.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    void testUpdateService_invalidPrice() {
        Service_DTO dto = new Service_DTO("NewName", -1, 0);
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(svc1));
        when(serviceRepository.existsByServiceName("NewName")).thenReturn(false);

        ResponseEntity<?> resp = controller.updateService(1L, dto);
        assertThat(resp.getStatusCodeValue()).isEqualTo(400);
        assertThat(resp.getBody()).isInstanceOf(JSOn_objectString.class);
    }

    // --- deleteService ---
    @Test
    void testDeleteService_success() {
        when(serviceRepository.existsById(2L)).thenReturn(true);

        ResponseEntity<?> resp = controller.deleteService(2L);
        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
        verify(serviceRepository).deleteById(2L);
    }

    @Test
    void testDeleteService_notFound() {
        when(serviceRepository.existsById(99L)).thenReturn(false);

        ResponseEntity<?> resp = controller.deleteService(99L);
        assertThat(resp.getStatusCodeValue()).isEqualTo(404);
    }

    // --- getServicesByPriceRange ---
    @Test
    void testGetServicesByPriceRange_filtersCorrectly() {
        when(serviceRepository.findAll()).thenReturn(Arrays.asList(svc1, svc2));
        List<Service_DTO> dtos = controller.getServicesByPriceRange(22.0, 30.0);

        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).getService_name()).isEqualTo("Pedicure");
    }

    // --- estimateTotalPrice ---
    @Test
    void testEstimateTotalPrice_success() {
        when(serviceRepository.findAllById(Arrays.asList(1L,2L)))
                .thenReturn(Arrays.asList(svc1, svc2));

        ResponseEntity<?> resp = controller.estimateTotalPrice(Arrays.asList(1L,2L));
        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
        //assertThat(((JSOn_objectString)resp.getBody()).getValue())
                //.contains("Total estimated price: 45.0");
    }

    @Test
    void testEstimateTotalPrice_missingService() {
        when(serviceRepository.findAllById(Arrays.asList(1L,2L)))
                .thenReturn(Collections.singletonList(svc1));

        ResponseEntity<?> resp = controller.estimateTotalPrice(Arrays.asList(1L,2L));
        assertThat(resp.getStatusCodeValue()).isEqualTo(400);
        assertThat(resp.getBody()).isInstanceOf(String.class);
    }
}
