package com.buabook.api_interface.local;

import org.json.JSONObject;

import com.buabook.api_interface.client.RequestErrorException;
import com.buabook.api_interface.enums.EBrokerCommands;
import com.buabook.api_interface.inbound.InboundRequest;

public interface ILocalCommandExecutor {

	public JSONObject handle(EBrokerCommands command, InboundRequest request) throws RequestErrorException;

}
