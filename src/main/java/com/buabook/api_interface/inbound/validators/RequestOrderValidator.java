package com.buabook.api_interface.inbound.validators;

import java.util.List;
import java.util.UUID;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.buabook.api_interface.client.RequestErrorException;
import com.buabook.api_interface.enums.EBrokerCommands;
import com.buabook.api_interface.enums.EBrokerError;
import com.buabook.api_interface.inbound.IJsonRequestValidator;
import com.google.common.collect.ImmutableList;

@Component
public class RequestOrderValidator implements IJsonRequestValidator {
	
	private static final List<String> REQUIRED_ARGUMENTS = ImmutableList.<String>builder()
																					.add("command_to_abort")
																					.add("uuid_to_abort")
																					.build();

	@Override
	public JSONObject validateForCommand(JSONObject arguments) throws RequestErrorException {	
		if((! arguments.has("command_to_abort")) || ! arguments.has("uuid_to_abort"))
			throw new RequestErrorException(EBrokerError.BAD_ARGUMENTS);
		
		String commandToAbortStr = arguments.getString("command_to_abort");
		String uuidToAbortStr = arguments.getString("uuid_to_abort");
		
		try {
			EBrokerCommands.valueOf(commandToAbortStr);
			UUID.fromString(uuidToAbortStr);
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
