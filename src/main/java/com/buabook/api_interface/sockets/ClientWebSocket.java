package com.buabook.api_interface.sockets;

import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.buabook.api_interface.RequestQueueController;
import com.buabook.api_interface.client.Broadcaster;
import com.buabook.api_interface.query.InitialClientConnectionQuerier;


@WebSocket
public class ClientWebSocket {
	private static final Logger log = Logger.getLogger(ClientWebSocket.class);
	
	private static final Long CLIENT_WS_IDLE_TIMEOUT_MS = 0l;
	
	
	private final RequestQueueController requestQueue;
	
	private final InitialClientConnectionQuerier onConnectQuerier;

	
	public Session session;
	
	
	public ClientWebSocket(InitialClientConnectionQuerier onConnectQuerier, RequestQueueController requestQueue){
		this.onConnectQuerier = onConnectQuerier;
		this.requestQueue = requestQueue;
	}
	

	@OnWebSocketClose
	public void onClose(int statusCode, String reason) {    	
		log.info("Broker Client WebSocket closed: (" +  statusCode + ") " + reason);
		Broadcaster.getInstance().unsubscribe(this);
	}
	
	@OnWebSocketError
	public void onError(Throwable t) {
		log.error("Broker Client WebSocket error: " + t.getMessage(), t);
	}
	
	@OnWebSocketConnect
	public void onConnect(Session newWebSocket) {
		this.session = newWebSocket;
		
		log.info("Broker Client WebSocket open: " + session.getRemoteAddress());
		session.setIdleTimeout(CLIENT_WS_IDLE_TIMEOUT_MS);
		
		Broadcaster.getInstance().subscribe(this);
		onConnectQuerier.queryApiOnClientConnect();
	}
	
	@OnWebSocketMessage
	public void onMessage(String message) {
		log.debug("Incoming client message: " + message);
		requestQueue.addNewRequest(message);
	}

}