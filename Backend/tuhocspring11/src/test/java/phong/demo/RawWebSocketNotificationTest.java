package phong.demo;

import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import phong.demo.Websocket.RawWebSocketNotification;

import java.io.IOException;

import static org.mockito.Mockito.*;

class RawWebSocketNotificationTest {

    private RawWebSocketNotification notification;
    private Session mockSession;
    private RemoteEndpoint.Basic mockBasic;

    @BeforeEach
    void setUp() {
        notification = new RawWebSocketNotification();

        mockSession = mock(Session.class);
        mockBasic = mock(RemoteEndpoint.Basic.class);

        when(mockSession.getId()).thenReturn("mock-session-1");
        when(mockSession.getBasicRemote()).thenReturn(mockBasic);
    }

    @Test
    void testOnOpen_addsSession() {
        notification.onOpen(mockSession);
        RawWebSocketNotification.sendToAll("Hello");

        try {
            verify(mockBasic).sendText("Hello");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testOnMessage_logsMessage() {
        notification.onMessage("test-message", mockSession);
        // No actual assertion needed since it only prints to console
    }

    @Test
    void testOnClose_removesSession() {
        notification.onOpen(mockSession);
        notification.onClose(mockSession);

        RawWebSocketNotification.sendToAll("After close");
        try {
            verify(mockBasic, never()).sendText(any());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSendToAll_handlesIOException() throws IOException {
        RemoteEndpoint.Basic badBasic = mock(RemoteEndpoint.Basic.class);
        Session badSession = mock(Session.class);
        when(badSession.getId()).thenReturn("bad-session");
        when(badSession.getBasicRemote()).thenReturn(badBasic);

        doThrow(new IOException("Send failed")).when(badBasic).sendText(any());

        notification.onOpen(badSession);
        RawWebSocketNotification.sendToAll("test");

        verify(badBasic).sendText("test"); // IOException will be printed
    }
}

