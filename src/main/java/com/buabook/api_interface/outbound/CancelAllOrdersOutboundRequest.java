package com.buabook.api_interface.outbound;

import org.json.JSONObject;

import com.buabook.api_interface.enums.EBrokerCommands;
import com.buabook.api_interface.enums.ERequestMethod;
import com.buabook.http.common.HttpHelpers;

public class CancelAllOrdersOutboundRequest extends ApiOutboundRequest {
	
	private static final String API_URL = "orders/cancel/all";
	
	
	public CancelAllOrdersOutboundRequest() {
		super(EBrokerCommands.CANCEL_ALL, ERequestMethod.GET, API_URL);
	}

	@Override
	public void addToCommandArguments(JSONObject object) {
		// Any extra cancel all requests don't change anything
		return;
	}
	
	@Override
	public String getRequestUrl() {
		return HttpHelpers.appendUrlParameters(super.getRequestUrl(), getCommandArguments().toMap());
	}

}
