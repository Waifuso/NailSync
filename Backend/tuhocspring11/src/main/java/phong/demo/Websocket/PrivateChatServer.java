package phong.demo.Websocket;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@ServerEndpoint("/ws/chat/{sender}/to/{recipient}")
@Component
public class PrivateChatServer {


    private static Map<String, Map<String, Session>> conversationMap = new Hashtable<>();

    private final Logger logger = LoggerFactory.getLogger(PrivateChatServer.class);

    private static final List<String> bannedWords = List.of(
            "fuck", "ugly", "stupid"

    );

    private boolean containsBannedWord(String message) {
        for (String banned : bannedWords) {
            if (message.toLowerCase().contains(banned.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("sender") String sender, @PathParam("recipient") String recipient) throws IOException {

        // check if either one has bug to debug
        if (sender == null || recipient == null) {

            logger.error("Connection rejected: sender or recipient is null. Sender: " + sender + ", Recipient: " + recipient);

            session.getBasicRemote().sendText("Invalid connection parameters.");

            session.close();

            return;
        }
        String conversationKey = getConversationKey(sender, recipient);

        logger.info("[onOpen] " + sender + " connected for conversation: " + conversationKey);

        Map<String, Session> sessions = conversationMap.get(conversationKey);

        if (sessions == null) {
            sessions = new Hashtable<>();
            conversationMap.put(conversationKey, sessions);
        }

        if (sessions.containsKey(sender)) {
            session.getBasicRemote().sendText("Sender already connected for this conversation");
            session.close();
            return;
        }
        sessions.put(sender, session);
        if (sessions.size() == 2) {
            broadcast(conversationKey, "Both users are now connected. You may start chatting.");
        }
    }


    @OnMessage
    public void onMessage(Session session, String message, @PathParam("sender") String sender, @PathParam("recipient") String recipient) throws IOException {
        String conversationKey = getConversationKey(sender, recipient);

        logger.info("[onMessage] " + sender + " in conversation " + conversationKey + ": " + message);

        if (containsBannedWord(message)) {
            session.getBasicRemote().sendText("⚠️ Your message contains inappropriate language and was not sent.");
            return;
        }

        Map<String, Session> sessions = conversationMap.get(conversationKey);

        if (sessions != null) {

            for (Map.Entry<String, Session> entry : sessions.entrySet()) {

                if (!entry.getKey().equals(sender)) {

                    entry.getValue().getBasicRemote().sendText(sender + ": " + message);
                }
            }
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("sender") String sender, @PathParam("recipient") String recipient) throws IOException {
        if (sender == null || recipient == null) {
            logger.error("Cannot determine conversation key because one of the parameters is null. sender: " + sender + ", recipient: " + recipient);
            return;
        }
        String conversationKey = getConversationKey(sender, recipient);
        logger.info("[onClose] " + sender + " in conversation " + conversationKey);

        Map<String, Session> sessions = conversationMap.get(conversationKey);
        if (sessions != null) {
            sessions.remove(sender);
            if (sessions.isEmpty()) {

                conversationMap.remove(conversationKey);

            } else {
                broadcast(conversationKey, sender + " has disconnected.");
            }
        }
    }



    @OnError
    public void onError(Session session, Throwable throwable, @PathParam("sender") String sender, @PathParam("recipient") String recipient) {
        String conversationKey = getConversationKey(sender, recipient);
        logger.error("[onError] " + sender + " in conversation " + conversationKey + ": " + throwable.getMessage());
    }

    private String getConversationKey(String user1, String user2) {
        if (user1 == null || user2 == null) {
            logger.error("One of the users is null. user1: " + user1 + ", user2: " + user2);

            return "unknown-conversation";
        }

        return (user1.compareTo(user2) <= 0) ? user1 + "-" + user2 : user2 + "-" + user1;
    }


    private void broadcast(String conversationKey, String message) {
        Map<String, Session> sessions = conversationMap.get(conversationKey);
        if (sessions != null) {
            for (Session session : sessions.values()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    logger.error("Error broadcasting message: " + e.getMessage());
                }
            }
        }
    }
}