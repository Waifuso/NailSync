package phong.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import phong.demo.Controller.ChatBotController;
import phong.demo.DTO.JSOn_objectString;
import phong.demo.Service.ChatGptService;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

class ChatBotControllerTest {

    @Mock
    private ChatGptService chatGptService;

    @InjectMocks
    private ChatBotController chatBotController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ✅ Test: Successful response from ChatGPT
    @Test
    void testAsk_ReturnsResponse() {
        String question = "What is NAILSYNC?";
        String answer = "NAILSYNC is a nail salon assistant.";

        when(chatGptService.sendMessage(question, 1L))
                .thenReturn(Mono.just(answer));

        Mono<ResponseEntity<JSOn_objectString>> result = chatBotController.ask(question, 1L);


    }

    // ✅ Test: Empty response
    @Test
    void testAsk_EmptyResponse_ReturnsNoContent() {
        String question = "Will I get a reply?";
        when(chatGptService.sendMessage(question, 2L)).thenReturn(Mono.empty());

        Mono<ResponseEntity<JSOn_objectString>> result = chatBotController.ask(question, 2L);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCodeValue() == 204)
                .verifyComplete();
    }

    // ❌ Test: Error occurs in service
    @Test
    void testAsk_ErrorResponse_Returns500() {
        String question = "Crash the bot";

        when(chatGptService.sendMessage(question, 3L))
                .thenReturn(Mono.error(new RuntimeException("Simulated failure")));

        Mono<ResponseEntity<JSOn_objectString>> result = chatBotController.ask(question, 3L);

//        StepVerifier.create(result)
//                .expectNextMatches(response ->
//                        response.getStatusCodeValue() == 500 &&
//                                response.getBody()
//                )
//                .verifyComplete();
    }
}
