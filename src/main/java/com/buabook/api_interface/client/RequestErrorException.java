package com.buabook.api_interface.client;

import com.buabook.api_interface.enums.EBrokerError;

public class RequestErrorException extends Exception {
	private static final long serialVersionUID = -205843329072438985L;
	
	private static final String message = "The inbound request had an error which caused the broker to reject it";

	
	private final EBrokerError error;
	
	
	public RequestErrorException(EBrokerError error) {
		super(message);
		
		this.error = error;
	}
	
	
	public EBrokerError getError() {
		return error;
	}

}
