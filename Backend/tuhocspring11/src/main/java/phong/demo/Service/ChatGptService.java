package phong.demo.Service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;


import org.springframework.web.reactive.function.client.WebClient;
import phong.demo.DTO.ChatGPTRequest;
import phong.demo.DTO.ChatResponse;
import phong.demo.DTO.chatMessage;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatGptService {


    private final Logger logger = LoggerFactory.getLogger(ChatGptService.class);

    @Autowired
    private Userservice userservice;

    @Autowired
    private  DailyFinanceService dailyFinanceService;

    private WebClient webClient;

    @Autowired
    private MessageAnalyze messageAnalyze;

    public ChatGptService(@Qualifier("openAI") WebClient webClient) {
        this.webClient = webClient;
    }

    private  String model = "gpt-3.5-turbo";

    public Mono<String> sendMessage(String UserMessage,long id){

       String hint = messageAnalyze.Analyze(UserMessage);

       String extraInformation = messageAnalyze.extraInformationUser(id);

       var request = new ChatGPTRequest(model, List.of(new chatMessage("system","you are a friendly chatbot assistant support for a nail salon called NailSync please answer with respectful tone, Please answer less than 200 words and alway start with hello the user name I give you."),
                                                        new chatMessage("system","When it come to the question related to database like appointment,employee,and service use  provided information: " + hint ),
                                                        new chatMessage("system","Here is the extra information about the person you talk to: " + extraInformation),
                                                        new chatMessage("system"," if they use foreign language answer user the foreign language: ") ,
                                                        new chatMessage("user",UserMessage)),1,200,1);

        return webClient.post()
                .uri("/chat/completions")                              // path API
                .httpRequest(r ->                                      // in URL để debug
                        System.out.println("URL gửi đi: " + r.getURI()))
                .bodyValue(request)                                        // JSON body
                .retrieve()
                .onStatus(org.springframework.http.HttpStatusCode::isError,
                        resp -> resp.bodyToMono(String.class)
                                .flatMap(body ->
                                        Mono.error(new RuntimeException("OpenAI: " + body))))
                .bodyToMono(ChatResponse.class)
                .map(res -> res.getChoiceList()
                        .get(0)
                        .getChatMessage()
                        .getContent());


    }



    public Mono<String> DataAnalyze(){

        ZoneId iowaZone = ZoneId.of("America/Chicago");
        LocalDate today = LocalDate.now(iowaZone);

        List<Map<LocalDate,Double>> IncomesAndDates = new ArrayList<>();

        List<LocalDate> Dates = new ArrayList<>();


        for (int i = 0; i < 6; i++) {

            double income = 0;

            income = dailyFinanceService.getFinance(today);

            Map<LocalDate,Double> DailyReport = new HashMap<>();

            DailyReport.put(today,income);

            IncomesAndDates.add(DailyReport);

            Dates.add(today);

            today = today.minusDays(1);
        }

        int NewUserCount = 5;

        logger.info(NewUserCount  + IncomesAndDates.toString());

        var request = new ChatGPTRequest(model, List.of(new chatMessage("system","you are the data analyze of the Nailsync please analyze the report of the week"),
                new chatMessage("user","Here is the information related to the revenue" + IncomesAndDates.toString() + "here is the infomrmation relate to the new user"+ NewUserCount)),1,300,1);

        return webClient.post()
                .uri("/chat/completions")                              // path API
                .httpRequest(r ->                                      // in URL để debug
                        System.out.println("URL gửi đi: " + r.getURI()))
                .bodyValue(request)                                        // JSON body
                .retrieve()
                .onStatus(org.springframework.http.HttpStatusCode::isError,
                        resp -> resp.bodyToMono(String.class)
                                .flatMap(body ->
                                        Mono.error(new RuntimeException("OpenAI: " + body))))
                .bodyToMono(ChatResponse.class)
                .map(res -> res.getChoiceList()
                        .get(0)
                        .getChatMessage()
                        .getContent());


    }



}
