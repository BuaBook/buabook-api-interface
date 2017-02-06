package com.buabook.api_interface.outbound;

import java.util.List;

import org.json.JSONObject;

import com.buabook.api_interface.enums.EBrokerCommands;
import com.buabook.api_interface.enums.ERequestMethod;

public class CancelOrderOutboundRequest extends ApiOutboundRequest {
	
	private static final String API_URL = "orders/cancel";

	public CancelOrderOutboundRequest() {
		super(EBrokerCommands.ORDER_CANCEL, ERequestMethod.POST, API_URL);
	}

	@Override
	public void addToCommandArguments(JSONObject object) {
		List<Object> existing = commandArguments.getJSONArray("order_ids").toList();
		existing.addAll(object.getJSONArray("order_ids").toList());

		commandArguments.put("order_ids", existing);
	}

}
