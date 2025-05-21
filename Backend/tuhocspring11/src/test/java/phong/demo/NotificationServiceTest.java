package phong.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import phong.demo.Service.NotificationService;

import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Mock
    private SimpMessagingTemplate template;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendNotification_SendsToCorrectDestination() {
        String message = "Hello, this is a test notification";

        notificationService.sendNotification(message);

        verify(template).convertAndSend("/topic/notifications", message);
    }
}
