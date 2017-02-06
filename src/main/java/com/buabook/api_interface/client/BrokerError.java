package com.buabook.api_interface.client;

import java.util.UUID;

import org.json.JSONObject;

import com.buabook.api_interface.enums.EBrokerError;
import com.buabook.api_interface.enums.EBrokerResponse;

public class BrokerError {
	
	public static JSONObject build(EBrokerError error, Integer botId, UUID botRequestId) {
		if(error == null)
			throw new IllegalArgumentException("Must pass an error to build the error JSON");
		
		JSONObject errorResponse = new JSONObject()
											.put("exception", error.getDescription())
											.put("response", JSONObject.NULL)
											.put("success", false)
											.put("errorCode", error);
		
		if(botId != null)
			errorResponse.put("bot_id", botId);
		
		if(botRequestId != null)
			errorResponse.put("bot_request_id", botRequestId.toString());
		
		return new JSONObject()
							.put("responseType", EBrokerResponse.COMMAND_ERROR)
							.put("response", errorResponse);	
	}
}
