package com.buabook.api_interface.inbound.validators;

import java.util.List;

import org.json.JSONObject;

import com.buabook.api_interface.client.RequestErrorException;
import com.buabook.api_interface.enums.EBrokerError;
import com.buabook.api_interface.enums.EProductType;
import com.buabook.api_interface.inbound.IJsonRequestValidator;
import com.google.common.collect.ImmutableList;

public class CancelAllValidator implements IJsonRequestValidator {

	private static final List<String> REQUIRED_ARGUMENTS = ImmutableList.<String>builder()
																						.add("product_type")
																						.build();

	@Override
	public JSONObject validateForCommand(JSONObject arguments) throws RequestErrorException {
		String productType = arguments.getString("product_type");
		
		try {
			EProductType.valueOf(productType);
		} catch(IllegalArgumentException e) {
			throw new RequestErrorException(EBrokerError.BAD_ARGUMENTS);
		}
		
		return null;
	}

	@Override
	public List<String> getRequiredArgumentKeys() {
		return REQUIRED_ARGUMENTS;
	}

}
