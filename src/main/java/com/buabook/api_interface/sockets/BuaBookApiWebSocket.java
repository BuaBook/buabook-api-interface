package com.buabook.api_interface.sockets;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.json.JSONObject;

import com.buabook.api_interface.api.BuaBookApiAccess;
import com.buabook.api_interface.client.Broadcaster;
import com.buabook.api_interface.streaming.StreamingUpdateConverters;
import com.buabook.http.common.HttpHelpers;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

@WebSocket(maxTextMessageSize = 2 * 1024 * 1024)
public class BuaBookApiWebSocket {
	private static final Logger log = Logger.getLogger(BuaBookApiWebSocket.class);
	
	private static final Long CONNECT_WAIT_MS = 2000l;
	
	private static final Long WS_IDLE_TIMEOUT_MS = 12 * 60 * 60 * 1000l;
	
	private static final List<String> CONNECT_FAILURE_REASONS = ImmutableList.<String>builder()
																							.add("USER_LOGIN_FAILED")
																							.add("API_USER_LOGGED_OUT")
																							.build();
	
	
	private final String webSocketUrl;
	
	private final BuaBookApiAccess apiAccess;
	
	private final WebSocketClient webSocketClient;
	
	
	private Session webSocketSession;
	
	
	public BuaBookApiWebSocket(String wsUrl, BuaBookApiAccess apiAccess, QueuedThreadPool webSocketThreadPool) throws Exception {
		if(Strings.isNullOrEmpty(wsUrl) || apiAccess == null)
			throw new IllegalArgumentException("Must supply WebSocket URL and connection object");
		
		this.webSocketUrl = wsUrl;
		this.apiAccess = apiAccess;
		
		this.webSocketClient = new WebSocketClient(new SslContextFactory(), webSocketThreadPool);
		this.webSocketClient.setMaxIdleTimeout(WS_IDLE_TIMEOUT_MS);
		this.webSocketClient.start();
	}
	

	@OnWebSocketConnect
    public void onConnect(Session sess) {
        log.info("API WebSocket open [ Target: " + sess.getRemoteAddress() + " ]");
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
    	log.warn("WebSocket closed " + statusCode + " " + reason);
    	
    	if(CONNECT_FAILURE_REASONS.contains(reason)) {
    		log.warn("WebSocket closure indicates session has expired. Clearing credentials.");
    		apiAccess.invalidateAccess();
    		
    		return;
    	}
    	
    	connect();
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
    	log.warn(cause);
    }

    @OnWebSocketMessage
    public void onMessage(String msg) {
    	JSONObject jsonForClient = StreamingUpdateConverters.convert(new JSONObject(msg));
    	Broadcaster.getInstance().broadcast(jsonForClient);
    }

    
    public Session getWebSocketSession() {
    	return webSocketSession;
    }
    
    public void reconnect() {
    	if(webSocketSession != null)
    		if(webSocketSession.isOpen())
    			close(StatusCode.SERVICE_RESTART, "RELOGIN");
    	
    	connect();
    }
    
    public void close(int statusCode, String reason) {
    	log.info("Closing existing WebSocket session [ Target: " + webSocketSession.getUpgradeRequest().getRequestURI() + " ]");
    	
    	if(statusCode == 0 || Strings.isNullOrEmpty(reason))
    		webSocketSession.close();
    	else 
    	  	webSocketSession.close(statusCode, reason);
    }

    
    private synchronized void connect() {
    	log.info("Attempting to open new WebSocket to BuaBook API [ URL: " + webSocketUrl + " ] [ Credentials: " + apiAccess.getCredentialsAsMap() + " ]");
    	
    	while(! apiAccess.accessAvailable()) {
			log.warn("API access is not available. Waiting for access...");
			
			try { 
	    		Thread.sleep(CONNECT_WAIT_MS); 
	    	} catch (InterruptedException e) {}
		}
		
		URI webSocketURI = null;
		
		try {
			String url = HttpHelpers.appendUrlParameters(webSocketUrl, apiAccess.getCredentialsAsMap());
			webSocketURI = new URI(url);
		} catch (URISyntaxException e) {
			log.error("WebSocket URL could not be built due to syntax error. Error - " + e.getMessage());
			throw new IllegalArgumentException("Bad WebSocket URL");
		}
		
		try {
			webSocketSession = webSocketClient.connect(this, webSocketURI).get();
		} catch (InterruptedException | ExecutionException | IOException e) {
			log.error("Failed to connect to BuaBook API via WebSocket. Error - " + e.getMessage());
			
			try { 
	    		Thread.sleep(CONNECT_WAIT_MS); 
	    	} catch (InterruptedException e1) {}
			
			connect();
		}
    }
}