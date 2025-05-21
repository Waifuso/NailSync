package phong.demo;


import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import phong.demo.Websocket.PrivateChatServer;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import static org.mockito.Mockito.*;

class PrivateChatServerTest {

    private PrivateChatServer chatServer;
    private Session mockSession;
    private RemoteEndpoint.Basic mockBasic;

    @BeforeEach
    void setUp() {
        chatServer = new PrivateChatServer();
        mockSession = mock(Session.class);
        mockBasic = mock(RemoteEndpoint.Basic.class);

        when(mockSession.getBasicRemote()).thenReturn(mockBasic);
    }

    @Test
    void testOnOpen_newConversation_success() throws Exception {
        chatServer.onOpen(mockSession, "alice", "bob");

        verify(mockBasic, never()).sendText(contains("Invalid"));
    }

    @Test
    void testOnOpen_missingSenderOrRecipient_shouldCloseSession() throws Exception {
        chatServer.onOpen(mockSession, null, "bob");

        verify(mockBasic).sendText(contains("Invalid"));
        verify(mockSession).close();
    }

    @Test
    void testOnMessage_bannedWord_blocked() throws Exception {
        chatServer.onOpen(mockSession, "alice", "bob"); // ensure conversation map is initialized

        // simulate 2nd participant joining
        Session mockRecipientSession = mock(Session.class);
        RemoteEndpoint.Basic mockRecipientBasic = mock(RemoteEndpoint.Basic.class);
        when(mockRecipientSession.getBasicRemote()).thenReturn(mockRecipientBasic);
        chatServer.onOpen(mockRecipientSession, "bob", "alice");

        chatServer.onMessage(mockSession, "you are stupid", "alice", "bob");

        verify(mockBasic).sendText(contains("inappropriate"));
      //  verify(mockRecipientBasic, never()).sendText(any());
    }

    @Test
    void testOnMessage_valid_shouldBroadcast() throws Exception {
        chatServer.onOpen(mockSession, "alice", "bob");

        Session mockRecipientSession = mock(Session.class);
        RemoteEndpoint.Basic mockRecipientBasic = mock(RemoteEndpoint.Basic.class);
        when(mockRecipientSession.getBasicRemote()).thenReturn(mockRecipientBasic);

        chatServer.onOpen(mockRecipientSession, "bob", "alice");

        chatServer.onMessage(mockSession, "hello bob", "alice", "bob");

        verify(mockRecipientBasic).sendText("alice: hello bob");
    }

    @Test
    void testOnClose_removesUser() throws Exception {
        chatServer.onOpen(mockSession, "alice", "bob");
        chatServer.onClose(mockSession, "alice", "bob");

        // internal map cleanup check indirectly by verifying no broadcast attempt (since only 1 user)
        verify(mockBasic, never()).sendText(contains("disconnected"));
    }
}
