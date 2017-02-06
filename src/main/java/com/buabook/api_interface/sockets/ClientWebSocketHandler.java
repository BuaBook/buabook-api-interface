package com.buabook.api_interface.sockets;

import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.buabook.api_interface.RequestQueueController;
import com.buabook.api_interface.query.InitialClientConnectionQuerier;

@Service
public class ClientWebSocketHandler extends WebSocketHandler {
	
	private static final Long WS_IDLE_TIMEOUT_MS = 12 * 60 * 60 * 1000l;
	
	
	@Autowired
	private InitialClientConnectionQuerier onClientConnectQuerier;
	
	@Autowired
	private RequestQueueController requestQueueController;
	
	
	@Override
	public void configure(WebSocketServletFactory factory) {
		
		factory.getPolicy().setIdleTimeout(WS_IDLE_TIMEOUT_MS);
		factory.setCreator(new WebSocketCreator() {
			@Override
			public Object createWebSocket(ServletUpgradeRequest request, ServletUpgradeResponse response) {
				return new ClientWebSocket(onClientConnectQuerier, requestQueueController);
			}
		});
	}
}
