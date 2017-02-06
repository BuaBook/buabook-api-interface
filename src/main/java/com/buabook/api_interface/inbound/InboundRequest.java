package com.buabook.api_interface.inbound;

import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.buabook.api_interface.client.BrokerError;
import com.buabook.api_interface.client.RequestErrorException;
import com.buabook.api_interface.enums.EBrokerCommands;
import com.buabook.api_interface.enums.EBrokerError;
import com.google.common.base.Strings;

public class InboundRequest {
	private static final Logger log = Logger.getLogger(InboundRequest.class);
	

	private EBrokerCommands command;
	
	private Integer clientId;
	
	private UUID clientRequestId;
	
	private JSONObject commandArguments;
	
	private EBrokerError clientError;

	
	public InboundRequest(EBrokerError error, JSONObject commandArguments) {
		this.clientError = error;
		this.commandArguments = commandArguments;
	}
	
	public InboundRequest(EBrokerError error, Integer clientId, UUID clientRequestId, JSONObject commandArguments) {
		this.clientError = error;
		this.clientId = clientId;
		this.clientRequestId = clientRequestId;
		this.commandArguments = commandArguments;
	}
	
	public InboundRequest(EBrokerCommands command, Integer clientId, UUID clientRequestId, JSONObject commandArguments) {
		this.command = command;
		this.clientId = clientId;
		this.clientRequestId = clientRequestId;
		this.commandArguments = commandArguments;
	}
	
	
	public boolean isError() {
		return clientError != null;
	}
	
	public JSONObject asError() {
		if(! isError())
			return null;
		
		log.warn("Invalid message received, returning error to client [ Client ID: " + clientId + " ] [ Error: " + clientError + " ] [ Request ID: " + clientRequestId + " ]");
		
		return BrokerError.build(clientError, clientId, clientRequestId);
	}
	
	public EBrokerCommands getCommand() {
		return command;
	}

	public Integer getClientId() {
		return clientId;
	}
	
	public UUID getClientRequestId() {
		return clientRequestId;
	}

	public JSONObject getCommandArguments() {
		return commandArguments;
	}
	
	
	private InboundRequest setError(EBrokerError error, JSONObject argumentsWithError) {
		this.clientError = error;
		this.commandArguments = argumentsWithError;
		
		return this;
	}
	
	
	public static InboundRequest fromString(String request) {
		return fromString(request, null);
	}
	
	public static InboundRequest fromString(String request, Map<EBrokerCommands, IJsonRequestValidator> withValidators) {
		if(Strings.isNullOrEmpty(request))
			return null;
		
		JSONObject json = null;
		
		try {
			json = new JSONObject(request);
		} catch (JSONException e) {
			return new InboundRequest(EBrokerError.BADLY_FORMED_JSON, null);
		}
		
		
		if((! json.has("bot_id")) || ! (json.get("bot_id") instanceof Integer))
			return new InboundRequest(EBrokerError.MISSING_BOT_ID, json);
		
		if(! json.has("bot_request_id"))
			return new InboundRequest(EBrokerError.MISSING_REQUEST_ID, json);
		
		Integer botId = json.getInt("bot_id");
		UUID botRequestId = null;
		
		try{
			botRequestId = UUID.fromString(json.getString("bot_request_id"));
		} catch (IllegalArgumentException e){
			return new InboundRequest(EBrokerError.MISSING_REQUEST_ID, json);
		}
		
		InboundRequest parsedRequest = new InboundRequest((EBrokerCommands) null, botId, botRequestId, null);
		
		if(! json.has("command"))
			return parsedRequest.setError(EBrokerError.COMMAND_MISSING, json);

		if(! json.has("arguments"))
			return parsedRequest.setError(EBrokerError.ARGUMENTS_MISSING, json);
		
		EBrokerCommands command = null;
		
		try {
			command = EBrokerCommands.valueOf(json.getString("command"));
		} catch(IllegalArgumentException e) {
			return parsedRequest.setError(EBrokerError.INVALID_COMMAND, json);
		}
		
		JSONObject arguments = null;
		
		try {
			arguments = json.getJSONObject("arguments"); 
		} catch (JSONException e) {
			return parsedRequest.setError(EBrokerError.ARGUMENTS_MISSING, json);
		}
		
		if(withValidators != null && withValidators.containsKey(command)) {
			JSONObject validated = null;
			
			try {
				validated = withValidators.get(command).validate(arguments);
			} catch (RequestErrorException e) {
				return parsedRequest.setError(e.getError(), json);
			}
			
			if(validated != null)
				arguments = validated;
		}
		
		parsedRequest.command = command;
		parsedRequest.commandArguments = arguments;
		
		return parsedRequest;
	}
	
}
