package willydekeyser.config;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        sessions.remove(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    	
    	System.out.println("Message: " + message + " - " + message.getPayload());
    	JsonNode jsonNode = objectMapper.readTree(message.getPayload());
    	String userMessage = jsonNode.get("chatMessage-1").asString();
    	String message2 = jsonNode.get("chatMessage-2").asString();
        
        String htmlFragment = """
            <div id="message" class="message-box" hx-swap-oob="true">
                <h2>Message</h2>
                <p style="font-size: 24px; font-weight: bold; color: #2ecc71;">%s</p>
                <p>%s</p>
                <p style="font-size: 12px; color: gray;">Latest update: %s</p>
                <p>Session ID: %s</p>
                <p>Thread ID: %s</p>
            </div>
            """.formatted(userMessage, message2, java.time.LocalTime.now().toString().substring(0, 8), session.getId(), Thread.currentThread().threadId());

        for (WebSocketSession activeSession : sessions) {
            if (activeSession.isOpen()) {
                try {
                	activeSession.sendMessage(new TextMessage(htmlFragment));
                } catch (IOException e) {
                    sessions.remove(activeSession);
				}
            }
        }
    }
}