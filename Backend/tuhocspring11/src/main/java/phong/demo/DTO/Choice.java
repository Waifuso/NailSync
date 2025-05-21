package phong.demo.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Choice {

    // JSON key is "message" → bind it here
    @JsonProperty("message")
    private chatMessage chatMessage;

    public chatMessage getChatMessage() {
        return chatMessage;
    }

    public void setChatMessage(chatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }
}
