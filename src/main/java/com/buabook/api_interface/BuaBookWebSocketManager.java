package com.buabook.api_interface;

import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.buabook.api_interface.api.BuaBookApiAccess;
import com.buabook.api_interface.sockets.BuaBookApiWebSocket;

@Service
public class BuaBookWebSocketManager {
	
    @Autowired
    private BuaBookApiAccess apiAccess;
    
    @Autowired
    @Qualifier("bbWebSocketApiThreadPool")
    private QueuedThreadPool bbWebSocketThreadPool;
    
    
    @Value("${buabook.buabook-api.ws-url}")
    private String wsUrl;
    
    
    private BuaBookApiWebSocket currentWebSocket;
    
	
	public synchronized void openApiSocket() throws Exception {
		if(currentWebSocket == null)
			currentWebSocket = new BuaBookApiWebSocket(wsUrl, apiAccess, bbWebSocketThreadPool);
		
		currentWebSocket.reconnect();
	}
	
}
