package phong.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import phong.demo.DTO.JSOn_objectString;
import phong.demo.DTO.JSon_Object;
import phong.demo.Service.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@RestController
@RequestMapping(path = "/api/test" )
public class test {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private Serviceservice serviceservice;

    @Autowired
    private MessageAnalyze messageAnalyze;

    @Autowired
    private Userservice userservice;

    @Autowired
    private ChatGptService chatGptService;

    @GetMapping()
    public ResponseEntity<?> get(){

        return ResponseEntity.ok(serviceservice.getServiceList());
    }

    @PostMapping ("/analyze")
    public ResponseEntity<?> check(@RequestBody String message){

        String respond = messageAnalyze.Analyze(message);

        return ResponseEntity.ok(respond);
    }

    @GetMapping("/Userinformation/{id}")
    public ResponseEntity<?> GetString(@PathVariable long id){
        return ResponseEntity.ok(messageAnalyze.extraInformationUser(id));
    }

    @GetMapping("/newUser")
    public  ResponseEntity<?> GetTotaluser(LocalDate startDate, LocalDate endDate){

        int answer = userservice.findNewUser(startDate,endDate);

        return ResponseEntity.ok(new JSon_Object<>(answer));
    }

    @GetMapping("/check/analyze")
    public Mono<ResponseEntity<JSOn_objectString>> getAnalyzeReport() {
        return chatGptService.DataAnalyze()
                // On success wrap into 200 OK
                .map(reply -> ResponseEntity
                        .ok(new JSOn_objectString(reply)))
                // If the Mono completes empty, return 204 No Content
                .defaultIfEmpty(ResponseEntity.noContent().build())
                // On error, catch and return 500 with the exception message
                .onErrorResume(ex -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new JSOn_objectString("Error: " + ex.getMessage()))
                ));
    }
}
