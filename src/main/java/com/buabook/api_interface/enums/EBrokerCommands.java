package com.buabook.api_interface.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum EBrokerCommands {
	// NOTE: Enumeration order is priority order for sending requests
	CANCEL_ALL,
	ORDER_CANCEL,
	ORDER_NEW,
	REQUEST_ABORT;
	
	public static List<String> valuesToStrings(){
		return Arrays.asList(values()).stream()
										.map(Object::toString)
										.collect(Collectors.toList());
	}
	
	public static List<EBrokerCommands> apiCommands() {
		return Arrays.asList(CANCEL_ALL, ORDER_CANCEL, ORDER_NEW);
	}
	
	public static List<EBrokerCommands> localCommands() {
		return Arrays.asList(REQUEST_ABORT);
	}
}
