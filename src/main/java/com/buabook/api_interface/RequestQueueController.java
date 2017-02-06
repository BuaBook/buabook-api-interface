package com.buabook.api_interface;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.buabook.api_interface.client.Broadcaster;
import com.buabook.api_interface.client.RequestErrorException;
import com.buabook.api_interface.enums.EBrokerCommands;
import com.buabook.api_interface.enums.EBrokerError;
import com.buabook.api_interface.enums.EOrderType;
import com.buabook.api_interface.helpers.ResponseBuilder;
import com.buabook.api_interface.inbound.InboundRequest;
import com.buabook.api_interface.inbound.IJsonRequestValidator;
import com.buabook.api_interface.local.ILocalCommandExecutor;
import com.buabook.api_interface.outbound.ApiOutboundRequest;
import com.buabook.api_interface.outbound.CancelAllOrdersOutboundRequest;
import com.buabook.api_interface.outbound.CancelOrderOutboundRequest;
import com.buabook.api_interface.outbound.NewOrderOutboundRequest;
import com.google.common.collect.ImmutableMap;

@Component
public class RequestQueueController extends Thread {
	private static final Logger log = Logger.getLogger(RequestQueueController.class);
	
	private static final int THREAD_SLEEP_MS = 1000;
	
	
	private final Map<EBrokerCommands, ILocalCommandExecutor> localCommandExecutors =  ImmutableMap.<EBrokerCommands, ILocalCommandExecutor>builder()
																																			.put(EBrokerCommands.REQUEST_ABORT, new RequestAbortExecutor())
																																			.build();
	
	
	@Autowired
	@Qualifier("brokerRequestValidators")
	private Map<EBrokerCommands, IJsonRequestValidator> requestValidators;
	
	@Autowired
	@Qualifier("defaultOrderTypeConfig")
	private EOrderType defaultOrderType;
	
	@Autowired
	@Qualifier("defaultOrderMarginConfig")
	private Double defaultOrderMargin;
	

	private ConcurrentLinkedQueue<InboundRequest> requestQueue;
	
	private Map<EBrokerCommands, ApiOutboundRequest> toSendQueues;
	
	
	@PostConstruct
	private void initialise() {
		this.requestQueue = new ConcurrentLinkedQueue<>();
		this.toSendQueues = new ConcurrentHashMap<>();
		initialiseSendQueues();
		
		log.info("Default order configuration [ Order Type: " + defaultOrderType + " ] [ Margin Percent: " + defaultOrderMargin + " ]");
		
		this.setName("RequestManager");
		this.start();
	}
	
	
	public void addNewRequest(String requestJsonStr) {
		requestQueue.add(InboundRequest.fromString(requestJsonStr, requestValidators));
	}
	
	public Map<EBrokerCommands, ApiOutboundRequest> getNextOutboundRequests() {
		Map<EBrokerCommands, ApiOutboundRequest> latest = new EnumMap<>(EBrokerCommands.class);
		
		for(Entry<EBrokerCommands, ApiOutboundRequest> sendQueue : toSendQueues.entrySet())
			if(! sendQueue.getValue().isEmpty())
				latest.put(sendQueue.getKey(), sendQueue.getValue());
			
		initialiseSendQueues();
		
		return latest;
	}
	
	@Override
	public synchronized void start() {
		log.info("Starting inbound request queue controller");
		super.start();
	}
	
	@Override
	public void run() {
		while(true) {
			if(requestQueue.isEmpty()) {
				try {
					Thread.sleep(THREAD_SLEEP_MS);
				} catch (InterruptedException e) {}
				
				continue;
			}
			
			InboundRequest latestRequest = requestQueue.poll();
			
			if(latestRequest.isError()) {
				Broadcaster.getInstance().broadcast(latestRequest.asError());
				continue;
			}
			
			EBrokerCommands latestRequestCommand = latestRequest.getCommand();
			
			if(EBrokerCommands.localCommands().contains(latestRequestCommand)) {
				log.info("Executing local command from client [ Command: " + latestRequestCommand + " ] [ Client ID: " + latestRequest.getClientId() + " ] [ Request ID: " + latestRequest.getClientRequestId() + " ]" );
				
				JSONObject toReturn = null;
				
				try {
					toReturn = localCommandExecutors.get(latestRequestCommand).handle(latestRequestCommand, latestRequest);
				} catch (RequestErrorException e) {
					toReturn = new InboundRequest(e.getError(), latestRequest.getCommandArguments()).asError();
				}
				
				Broadcaster.getInstance().broadcast(toReturn);
				continue;
			}
			
			log.info("Adding request from client [ Command: " + latestRequestCommand + " ] [ Client ID: " + latestRequest.getClientId() + " ] [ Request ID: " + latestRequest.getClientRequestId() + " ]");
				
			toSendQueues.get(latestRequestCommand).addToRequest(latestRequest);
		}
	}
	
	
	private void initialiseSendQueues() {
		toSendQueues.put(EBrokerCommands.ORDER_NEW, new NewOrderOutboundRequest(defaultOrderMargin, defaultOrderType));
		toSendQueues.put(EBrokerCommands.ORDER_CANCEL, new CancelOrderOutboundRequest());
		toSendQueues.put(EBrokerCommands.CANCEL_ALL, new CancelAllOrdersOutboundRequest());
	}
	
	
	private class RequestAbortExecutor implements ILocalCommandExecutor {
		
		@Override
		public JSONObject handle(EBrokerCommands command, InboundRequest request) throws RequestErrorException {
			
			String commandToAbortStr = request.getCommandArguments().getString("command_to_abort");
			String uuidToAbortStr = request.getCommandArguments().getString("uuid_to_abort");
			
			EBrokerCommands commandToAbort = null;
			UUID uuidToAbort = null;
			
			try {
				commandToAbort = EBrokerCommands.valueOf(commandToAbortStr);
				uuidToAbort = UUID.fromString(uuidToAbortStr);
			} catch (IllegalArgumentException e) {
				throw new RequestErrorException(EBrokerError.BAD_ARGUMENTS);
			}
			
			log.info("Aborting client request [ Command: " + commandToAbortStr + " ] [ Request To Abort: " + uuidToAbortStr + " ]");

			toSendQueues.get(commandToAbort).removePendingRequest(uuidToAbort);

			return ResponseBuilder.build(request.getCommand(), uuidToAbort, 0, new JSONObject().put("success", true));
		}

	}
}