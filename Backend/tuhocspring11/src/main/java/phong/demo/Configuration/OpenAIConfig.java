package phong.demo.Configuration;

import lombok.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OpenAIConfig {

    private final String ApiKey = "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";

    @Bean("openAI")
    public WebClient openAiWebClient(WebClient.Builder builder) {

        return  builder
                .baseUrl("https://api.openai.com/v1")                  // <- chỉ host
                .defaultHeader(HttpHeaders.AUTHORIZATION,
                        "Bearer " + ApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE)       // <- header JSON
                .build();



    }
}

