package phong.demo.Controller;

import com.mysql.cj.xdevapi.JsonString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import phong.demo.DTO.JSOn_objectString;
import phong.demo.Service.ChatGptService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/chatBot")
public class ChatBotController {

    @Autowired
    private ChatGptService chatGptService;

    /**
     * Example:
     * POST /api/chatBot/ask
     */
    @PostMapping("/ask/{id}")
    public Mono<ResponseEntity<JSOn_objectString>> ask(@RequestBody String question, @PathVariable long id ) {


        return chatGptService.sendMessage(question,id)
                // Wrap the reply in 200 OK
                .map(reply -> ResponseEntity.ok(new JSOn_objectString(reply)))
                .defaultIfEmpty(ResponseEntity.noContent().build()) // no content return 204
                // On error, return 500 with the exception message
                .onErrorResume(ex ->
                        Mono.just(ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new JSOn_objectString("Error: " + ex.getMessage()))
                        )
                );
    }
}

