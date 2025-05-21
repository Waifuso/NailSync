package phong.demo.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ChatGPTRequest {
    private String model;
    private List<chatMessage> messages;
    private double temperature;

    // API expects "max_tokens" → bind to your maxToken
    @JsonProperty("max_tokens")
    private int maxToken;

    // API expects "top_p" → bind to your top_p
    @JsonProperty("top_p")
    private double top_p;

    public ChatGPTRequest(String model,
                          List<chatMessage> messages,
                          double temperature,
                          int maxToken,
                          double top_p) {
        this.model       = model;
        this.messages    = messages;
        this.temperature = temperature;
        this.maxToken    = maxToken;
        this.top_p       = top_p;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<chatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<chatMessage> messages) {
        this.messages = messages;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getMaxToken() {
        return maxToken;
    }

    public void setMaxToken(int maxToken) {
        this.maxToken = maxToken;
    }

    public double getTop_p() {
        return top_p;
    }

    public void setTop_p(double top_p) {
        this.top_p = top_p;
    }
}
