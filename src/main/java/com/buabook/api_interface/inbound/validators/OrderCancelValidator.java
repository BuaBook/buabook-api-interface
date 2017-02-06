package com.buabook.api_interface.inbound.validators;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.buabook.api_interface.client.RequestErrorException;
import com.buabook.api_interface.enums.EBrokerError;
import com.buabook.api_interface.inbound.IJsonRequestValidator;
import com.google.common.collect.ImmutableList;

public class OrderCancelValidator implements IJsonRequestValidator {

	private static final List<String> REQUIRED_ARGUMENTS = ImmutableList.<String>builder()
																						.add("order_ids")
																						.build();

	@Override
	public JSONObject validateForCommand(JSONObject arguments) throws RequestErrorException {
		if(!(arguments.get("order_ids") instanceof JSONArray))
			throw new RequestErrorException(EBrokerError.BAD_ARGUMENTS);

		return null;
	}

	@Override
	public List<String> getRequiredArgumentKeys() {
		return REQUIRED_ARGUMENTS;
	}

}
