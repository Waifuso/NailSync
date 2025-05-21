package phong.demo.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ChatResponse {

    // JSON key is "choices" → bind it here
    @JsonProperty("choices")
    private List<Choice> choiceList;

    public List<Choice> getChoiceList() {
        return choiceList;
    }

    public void setChoiceList(List<Choice> choiceList) {
        this.choiceList = choiceList;
    }


}
