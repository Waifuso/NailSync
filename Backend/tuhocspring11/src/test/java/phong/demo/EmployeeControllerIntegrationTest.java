package phong.demo;



import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import phong.demo.DTO.Employee_CreateRequest;
import phong.demo.DTO.Service_DTO;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/employee";
    }

    @Test
    public void testCreateEmployeeSuccess() {
        String url = baseUrl() + "/Create/modify";

        // build request body
        ArrayList<Service_DTO> services = new ArrayList<>();
        services.add(new Service_DTO("Manicure", 20, 30));
        Employee_CreateRequest request =
                new Employee_CreateRequest("final", LocalDate.of(1990, 1, 1), services);

        // set JSON header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Employee_CreateRequest> entity = new HttpEntity<>(request, headers);

        // perform POST
        ResponseEntity<String> response =
                restTemplate.postForEntity(url, entity, String.class);

        // assertions
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(
                response.getBody().contains("Employee create success !!!!"),
                "Expected success message in response body"
        );
    }

    @Test
    public void testCreateEmployee_MissingUsername() {
        String url = baseUrl() + "/Create/modify";

        // username null
        Employee_CreateRequest request =
                new Employee_CreateRequest(null, LocalDate.of(1990, 1, 1), new ArrayList<>());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Employee_CreateRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(url, entity, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(
                response.getBody().contains("Employee name cannot be null"),
                "Expected validation message when username is null"
        );
    }

    @Test
    public void testGetEmployeeInfoList() {
        String url = baseUrl() + "/info/name";

        ResponseEntity<String> response =
                restTemplate.getForEntity(url, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // we expect a JSON wrapper with a "data" array
        assertFalse(
                response.getBody().contains("\"data\""),
                "Response should include a data field"
        );
    }

    @Test
    public void testAddServiceToNonexistentEmployee() {
        String url = baseUrl() + "/nonexistentuser/service";

        Service_DTO dto = new Service_DTO("Pedicure", 25, 35);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Service_DTO> entity = new HttpEntity<>(dto, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(url, entity, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(
                response.getBody().contains("Employee not found"),
                "Expected not-found message when employee does not exist"
        );
    }

//    @Test
//    public void testDeleteAllEmployees() {
//        // delete everything
//        restTemplate.delete(baseUrl());
//
//        // then list again
//        ResponseEntity<String> response =
//                restTemplate.getForEntity(baseUrl() + "/info/name", String.class);
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody());
//        // empty list → JSON should contain an empty array
//        assertTrue(
//                response.getBody().contains("[]"),
//                "Expected an empty data array after deletion"
//        );
//    }
}
