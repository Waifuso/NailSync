package phong.demo.Controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationController {
    @MessageMapping("/send")
    @SendTo("/topic/notifications")
    public String sendNotifications (String message) {
        System.out.println("Received message: " + message);
        return message;
    }

}
