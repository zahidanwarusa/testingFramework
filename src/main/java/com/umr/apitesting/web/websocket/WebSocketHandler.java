package com.umr.apitesting.web.websocket;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umr.apitesting.utils.LoggerUtil;

@Component
public class WebSocketHandler extends TextWebSocketHandler {
	private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		sessions.put(session.getId(), session);
		LoggerUtil.logInfo("WebSocket connection established: " + session.getId());
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) {
		try {
			String payload = message.getPayload();
			LoggerUtil.logInfo("Received message: " + payload);

			// Handle incoming message and send response
			session.sendMessage(new TextMessage("Received: " + payload));
		} catch (Exception e) {
			LoggerUtil.logError("Error handling WebSocket message", e);
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		sessions.remove(session.getId());
		LoggerUtil.logInfo("WebSocket connection closed: " + session.getId());
	}

	public void broadcastMessage(Object message) {
		try {
			String jsonMessage = objectMapper.writeValueAsString(message);
			TextMessage textMessage = new TextMessage(jsonMessage);

			sessions.values().forEach(session -> {
				try {
					if (session.isOpen()) {
						session.sendMessage(textMessage);
					}
				} catch (Exception e) {
					LoggerUtil.logError("Error broadcasting message to session: " + session.getId(), e);
				}
			});
		} catch (Exception e) {
			LoggerUtil.logError("Error broadcasting message", e);
		}
	}
}