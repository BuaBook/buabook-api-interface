package com.buabook.api_interface.helpers;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.json.JSONObject;

import com.buabook.api_interface.enums.EBrokerCommands;
import com.buabook.api_interface.enums.EBrokerResponse;

public final class ResponseBuilder {

	public static JSONObject build(EBrokerCommands command, UUID clientRequestId, Integer clientId, JSONObject apiResponse) {
		return build(command, Arrays.asList(clientRequestId), Arrays.asList(clientId), apiResponse);
	}
	
	public static JSONObject build(EBrokerCommands command, List<UUID> clientRequestIds, List<Integer> clientIds, JSONObject apiResponse) {
		EBrokerResponse responseType = null;
		
		if(apiResponse.getBoolean("success"))
			responseType = EBrokerResponse.RESPONSE;
		else
			responseType = EBrokerResponse.RESPONSE_ERROR;
		
		return build(command, clientRequestIds, clientIds, apiResponse, responseType);
	}
	
	public static JSONObject build(EBrokerCommands command, List<UUID> clientRequestIds, List<Integer> clientIds, JSONObject apiResponse, EBrokerResponse responseType) {
		return new JSONObject()
				.put("command", command)
				.put("bot_id", clientIds)
				.put("bot_request_id", clientRequestIds)
				.put("response", apiResponse)
				.put("response_type", responseType);
	}

}
