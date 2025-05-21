package phong.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import phong.demo.DTO.JSOn_objectString;
import phong.demo.DTO.JSon_Object;
import phong.demo.DTO.Service_DTO;
import phong.demo.Entity.Service;
import phong.demo.Repository.ServiceRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/services")
public class Service_Controller {

    @Autowired
    private ServiceRepository serviceRepository;

    @GetMapping
    public JSon_Object<List<Service_DTO>> getAllServices() {

        List<Service> services = serviceRepository.findAll();
        List<Service_DTO> serviceDtos = new ArrayList<>();


        for (Service service:services){

            Service_DTO serviceDto = new Service_DTO(service.getService_name(),service.getPrice(),service.getDuration());

            serviceDtos.add(serviceDto);
        }



        return new JSon_Object<>(serviceDtos);
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getServiceById(@PathVariable Long id) {

        return serviceRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createService(@RequestBody Service_DTO serviceDTO) {
        if (serviceDTO.getService_name() == null || serviceDTO.getService_name().trim().isEmpty() ||
                serviceDTO.getPrice() == null || serviceDTO.getPrice() < 0) {
            return ResponseEntity.badRequest().body("Invalid service data provided.");
        }
        if(serviceRepository.existsByServiceName(serviceDTO.getService_name())){

            return ResponseEntity.badRequest().body(new JSOn_objectString(" The service has already exist in database"));
        }

        Service service = new Service();

        service.setService_name(serviceDTO.getService_name());

        service.setPrice(serviceDTO.getPrice());

        Service savedService = serviceRepository.save(service);

        return ResponseEntity.ok(savedService);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateService(@PathVariable Long id, @RequestBody Service_DTO serviceDTO) {

        Optional<Service> Cur_Service = serviceRepository.findById(id);

        if (Cur_Service.isEmpty()){

            return ResponseEntity.notFound().build();
        }

        Service service = Cur_Service.get();



        if(serviceDTO.getService_name()== null
                || serviceRepository.existsByServiceName(serviceDTO.getService_name())){
            return ResponseEntity.badRequest().body(" The user name is already in used or null");
        }
        service.setService_name(serviceDTO.getService_name());

        if(serviceDTO.getPrice() == null || serviceDTO.getPrice() < 0){

            return ResponseEntity.badRequest().body(new JSOn_objectString(" the price is in valid: !"));
        }

        service.setPrice(serviceDTO.getPrice());

        Service save_service = serviceRepository.save(service);

        return ResponseEntity.ok(save_service);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteService(@PathVariable Long id) {
        if (!serviceRepository.existsById(id)) {

            return ResponseEntity.notFound().build();
        }
        serviceRepository.deleteById(id);
        return ResponseEntity.ok("Service deleted successfully.");
    }

    // for search function
    @GetMapping("/price-range")
    public List<Service_DTO> getServicesByPriceRange(@RequestParam Double minPrice, @RequestParam Double maxPrice) {
        return serviceRepository.findAll(  ).stream()
                .filter(service -> service.getPrice() >= minPrice && service.getPrice() <= maxPrice)
                .map(service -> new Service_DTO(service.getService_name(), service.getPrice(), service.getDuration()))
                .collect(Collectors.toList());
    }


    @PostMapping("/estimate")
    public ResponseEntity<?> estimateTotalPrice(@RequestBody List<Long> serviceIds) {

        List<Service> services = serviceRepository.findAllById(serviceIds);

        if (services.size() != serviceIds.size()) {

            return ResponseEntity.badRequest().body("One or more services not found.");
        }

        double totalPrice = services.stream()
                .mapToDouble(Service::getPrice)
                .sum();

        return ResponseEntity.ok(new JSOn_objectString("Total estimated price: " + totalPrice));
    }

    @DeleteMapping()
    public void deleteall(){
        serviceRepository.deleteAll();
    }


}










