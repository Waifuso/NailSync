package phong.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") // Make sure you have a test profile if needed
public class AdminControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/admin";
    }

    @Test
    public void testGetNumberOfUser() {
        ResponseEntity<Map> response = restTemplate.getForEntity(baseUrl() + "/usercount", Map.class);

        assertEquals(200, response.getStatusCodeValue());
        assertFalse(response.getBody().containsKey("data"));
    }

    @Test
    public void testGetNumberOfEmployee() {
        ResponseEntity<Map> response = restTemplate.getForEntity(baseUrl() + "/employee", Map.class);

        assertEquals(200, response.getStatusCodeValue());
        assertFalse(response.getBody().containsKey("data"));
    }

    @Test
    public void testGetFinanceEndpoint() {
        String date = LocalDate.now().toString();
        ResponseEntity<Map> response = restTemplate.getForEntity(baseUrl() + "/finance?localDate=" + date, Map.class);

        assertEquals(200, response.getStatusCodeValue());
        assertFalse(response.getBody().containsKey("data"));
    }

    @Test
    public void testSendReportWithNewDate() {
        String date = LocalDate.now().toString();
        ResponseEntity<Object> response = restTemplate.getForEntity(baseUrl() + "/total/income/viaEmail?localDate=" + date, Object.class);

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void testWeekRevenue() {
        ResponseEntity<Map> response = restTemplate.getForEntity(baseUrl() + "/week/revenue", Map.class);

        assertEquals(200, response.getStatusCodeValue());
        assertFalse(response.getBody().containsKey("data"));
    }
}
