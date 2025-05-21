package phong.demo;

import org.assertj.core.api.ListAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import phong.demo.Controller.test;
import phong.demo.DTO.JSOn_objectString;
import phong.demo.DTO.JSon_Object;
import phong.demo.Service.*;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TestControllerTest {

    @Mock private EmployeeService employeeService;
    @Mock private Serviceservice serviceservice;
    @Mock private MessageAnalyze messageAnalyze;
    @Mock private Userservice userservice;
    @Mock private ChatGptService chatGptService;

    @InjectMocks
    private test controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ✅ GET /api/test
    @Test
    void testGetServices() {
        //when(serviceservice.getServiceList()).thenReturn(List.of("Polish", "Pedicure"));

        ResponseEntity<?> response = controller.get();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        //ListAssert<?> contains = assertThat((List<?>) response.getBody()).contains("Polish", "Pedicure");
    }

    // ✅ POST /analyze
    @Test
    void testAnalyzeMessage() {
        String message = "How much for gel nails?";
        when(messageAnalyze.Analyze(message)).thenReturn("Pricing info for gel nails...");

        ResponseEntity<?> response = controller.check(message);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Pricing info for gel nails...");
    }

    // ✅ GET /Userinformation/{id}
    @Test
    void testExtraInformationUser() {
        when(messageAnalyze.extraInformationUser(1L)).thenReturn("User data");

        ResponseEntity<?> response = controller.GetString(1L);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("User data");
    }

    // ✅ GET /newUser
    @Test
    void testNewUserCount() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 12, 31);

        when(userservice.findNewUser(start, end)).thenReturn(15);

        ResponseEntity<?> response = controller.GetTotaluser(start, end);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        //assertThat(((JSon_Object<?>) response.getBody()).getList()).contains(15);
    }

    // ✅ GET /check/analyze (success)
    @Test
    void testAnalyzeReport_Success() {
        when(chatGptService.DataAnalyze()).thenReturn(Mono.just("Analysis done"));

        Mono<ResponseEntity<JSOn_objectString>> result = controller.getAnalyzeReport();

//        StepVerifier.create(result)
//                .expectNextMatches(response ->
//                        response.getStatusCode().is2xxSuccessful() &&
//                                response.getBody().getMess().equals("Analysis done")
//                )
//                .verifyComplete();
    }

    // ❌ GET /check/analyze (empty)
    @Test
    void testAnalyzeReport_Empty() {
        when(chatGptService.DataAnalyze()).thenReturn(Mono.empty());

        Mono<ResponseEntity<JSOn_objectString>> result = controller.getAnalyzeReport();

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCodeValue() == 204)
                .verifyComplete();
    }

    // ❌ GET /check/analyze (error)
    @Test
    void testAnalyzeReport_Error() {
        when(chatGptService.DataAnalyze())
                .thenReturn(Mono.error(new RuntimeException("AI crashed")));

        Mono<ResponseEntity<JSOn_objectString>> result = controller.getAnalyzeReport();

//        StepVerifier.create(result)
//                .expectNextMatches(response ->
//                        response.getStatusCodeValue() == 500 &&
//                                response.getBody().getMess().contains("AI crashed")
//                )
//                .verifyComplete();
    }
}

