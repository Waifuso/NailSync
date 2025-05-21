package phong.demo;


import org.junit.jupiter.api.Test;
import phong.demo.DTO.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Update_Employee__DTOTest {

    @Test
    void testConstructorAndGetters() {
        LocalDate dob = LocalDate.of(1990, 5, 15);

        Update_Employee__DTO dto = new Update_Employee__DTO(
                "john_doe",
                "john@example.com",
                "securePass123",
                true,
                dob
        );

        assertEquals("john_doe", dto.getUsername());
        assertEquals("john@example.com", dto.getEmail());
        assertEquals("securePass123", dto.getServicePassword());
        assertTrue(dto.isAvailable());
        assertEquals(dob, dto.getDob());
    }

    @Test
    void testSetters() {
        Update_Employee__DTO dto = new Update_Employee__DTO(null, null, null, false, null);

        dto.setUsername("alice");
        dto.setEmail("alice@example.com");
        dto.setServicePassword("newPass456");
        dto.setAvailable(true);
        LocalDate dob = LocalDate.of(2000, 1, 1);
        dto.setDob(dob);

        assertEquals("alice", dto.getUsername());
        assertEquals("alice@example.com", dto.getEmail());
        assertEquals("newPass456", dto.getServicePassword());
        assertTrue(dto.isAvailable());
        assertEquals(dob, dto.getDob());
    }

    @Test
    void testConstructorWithList() {
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        ServiceIds_DTO dto = new ServiceIds_DTO(ids);

        assertEquals(ids, dto.getServiceIds());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        ServiceIds_DTO dto = new ServiceIds_DTO();
        List<Long> ids = Arrays.asList(10L, 20L);

        dto.setServiceIds(ids);

        assertEquals(ids, dto.getServiceIds());
    }

    @Test
    void testEmptyList() {
        ServiceIds_DTO dto = new ServiceIds_DTO(List.of());
        assertTrue(dto.getServiceIds().isEmpty());
    }

    @Test
    void testChatMessageGetterSetter() {
        chatMessage msg = new chatMessage("user", "Hello");
        assertEquals("user", msg.getRole());
        assertEquals("Hello", msg.getContent());

        msg.setRole("assistant");
        msg.setContent("Hi there!");

        assertEquals("assistant", msg.getRole());
        assertEquals("Hi there!", msg.getContent());
    }

    @Test
    void testChoiceBinding() {
        chatMessage message = new chatMessage("assistant", "Response content");
        Choice choice = new Choice();
        choice.setChatMessage(message);

        assertEquals("assistant", choice.getChatMessage().getRole());
        assertEquals("Response content", choice.getChatMessage().getContent());
    }

    @Test
    void testChatResponseBinding() {
        chatMessage message = new chatMessage("assistant", "Hello from bot");
        Choice choice = new Choice();
        choice.setChatMessage(message);

        ChatResponse response = new ChatResponse();
        response.setChoiceList(List.of(choice));

        assertNotNull(response.getChoiceList());
        assertEquals(1, response.getChoiceList().size());
        assertEquals("Hello from bot", response.getChoiceList().get(0).getChatMessage().getContent());
    }

    @Test
    void testChatGPTRequestConstructorAndGetters() {
        chatMessage message = new chatMessage("user", "What is Java?");
        ChatGPTRequest request = new ChatGPTRequest(
                "gpt-4",
                List.of(message),
                0.7,
                100,
                1.0
        );

        assertEquals("gpt-4", request.getModel());
        assertEquals(1, request.getMessages().size());
        assertEquals("What is Java?", request.getMessages().get(0).getContent());
        assertEquals(0.7, request.getTemperature());
        assertEquals(100, request.getMaxToken());
        assertEquals(1.0, request.getTop_p());
    }

    @Test
    void testChatGPTRequestSetters() {
        ChatGPTRequest request = new ChatGPTRequest("gpt-4", List.of(), 0.0, 0, 0.0);

        request.setModel("gpt-3.5");
        request.setTemperature(0.9);
        request.setMaxToken(150);
        request.setTop_p(0.8);
        request.setMessages(List.of(new chatMessage("user", "Hi")));

        assertEquals("gpt-3.5", request.getModel());
        assertEquals(0.9, request.getTemperature());
        assertEquals(150, request.getMaxToken());
        assertEquals(0.8, request.getTop_p());
        assertEquals("Hi", request.getMessages().get(0).getContent());
    }
}
