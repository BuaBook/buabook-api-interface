package com.buabook.api_interface.client;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.json.JSONObject;

import com.buabook.api_interface.sockets.ClientWebSocket;

public class Broadcaster {
	private static final Logger log = Logger.getLogger(Broadcaster.class);

	private static final Broadcaster INSTANCE = new Broadcaster();
	
	
	private List<ClientWebSocket> subscribers = new CopyOnWriteArrayList<>();

	
	public static Broadcaster getInstance() {
		return INSTANCE;
	}
	
	public void subscribe(ClientWebSocket socket) {
		subscribers.add(socket);
	}
	
	public void unsubscribe(ClientWebSocket socket) {
		subscribers.remove(socket);
	}
	
	public synchronized void broadcast(JSONObject message){
		if(message == null)
			return;
		
		for(ClientWebSocket subscriber : subscribers){
			try{
				subscriber.session.getRemote().sendStringByFuture(message.toString());
			}catch(WebSocketException e){
				subscribers.remove(subscriber);
				log.error(e);
			}
		}
	}

}