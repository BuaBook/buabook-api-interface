package com.buabook.api_interface.outbound;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.JSONObject;

import com.buabook.api_interface.enums.EBrokerCommands;
import com.buabook.api_interface.enums.ERequestMethod;
import com.buabook.api_interface.helpers.ResponseBuilder;
import com.buabook.api_interface.inbound.InboundRequest;
import com.google.common.base.Strings;

public abstract class ApiOutboundRequest {
	
	private final EBrokerCommands command;
	
	private final ERequestMethod requestMethod;
	
	private final String requestUrl;
	
	
	private List<InboundRequest> clientRequests;
	
	protected JSONObject commandArguments;
	
	
	public ApiOutboundRequest(EBrokerCommands command, ERequestMethod requestMethod, String requestUrl) {
		if(command == null || requestMethod == null || Strings.isNullOrEmpty(requestUrl))
			throw new IllegalArgumentException("No null arguments permitted");
		
		this.command = command;
		this.requestMethod = requestMethod;
		this.requestUrl = requestUrl;
		
		this.clientRequests = new ArrayList<>();
	}
	
	public void addToRequest(InboundRequest clientRequest) {
		if(clientRequest == null || clientRequest.isError())
			return;
		
		clientRequests.add(clientRequest);
	}
	
	public EBrokerCommands getCommand() {
		return command;
	}
	
	public JSONObject getCommandArguments() {
		if(commandArguments != null)
			return commandArguments;
		
		for(InboundRequest request : clientRequests) {
			if(commandArguments == null)
				commandArguments = request.getCommandArguments();
			else
				addToCommandArguments(request.getCommandArguments());
		}
		
		return commandArguments;
	}
	
	public ERequestMethod getRequestMethod() {
		return requestMethod;
	}
	
	public String getRequestUrl() {
		return requestUrl;
	}
	
	
	public JSONObject getApiResponseForClient(JSONObject response) {	
		return ResponseBuilder.build(command, getRequestIds(), getClientIds(), response);
	}
	
	public List<UUID> getRequestIds() {
		return clientRequests.stream()
								.map(request -> request.getClientRequestId())
								.distinct()
								.collect(Collectors.toList());
	}
	
	public List<Integer> getClientIds() {
		return clientRequests.stream()
								.map(request -> request.getClientId())
								.distinct()
								.collect(Collectors.toList());
	}
	
	public void removePendingRequest(UUID botRequestToRemove) {
		clientRequests = clientRequests.stream()
											.filter(request -> ! request.getClientRequestId().equals(botRequestToRemove))
											.collect(Collectors.toList());
	}
	
	public boolean isEmpty() {
		return clientRequests.isEmpty();
	};

	
	protected abstract void addToCommandArguments(JSONObject object);
	
}
