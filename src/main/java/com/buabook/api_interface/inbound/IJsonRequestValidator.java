package com.buabook.api_interface.inbound;

import java.util.List;

import org.json.JSONObject;

import com.buabook.api_interface.client.RequestErrorException;
import com.buabook.api_interface.enums.EBrokerError;

public interface IJsonRequestValidator {

	/**
	 * 
	 * @param arguments
	 * @return The object post-validation with any erroneous elements removed or <code>null</code> if the original
	 * object should be used as is
	 * @throws RequestErrorException If there is any error with the object that should cause the request to be rejected
	 */
	public JSONObject validateForCommand(JSONObject arguments) throws RequestErrorException;
	
	
	public List<String> getRequiredArgumentKeys();
	
	
	default boolean requiredKeysArePresentInArgument(JSONObject object) {
		return object.keySet().containsAll(getRequiredArgumentKeys());
	}
	
	default JSONObject validate(JSONObject arguments) throws RequestErrorException {
		boolean argumentsPresent = arguments.keySet().containsAll(getRequiredArgumentKeys());
		
		if(! argumentsPresent)
			throw new RequestErrorException(EBrokerError.BAD_ARGUMENTS);
		
		return validateForCommand(arguments);
	}
}
