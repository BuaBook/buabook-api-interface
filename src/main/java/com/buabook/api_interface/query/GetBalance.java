package com.buabook.api_interface.query;

import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.buabook.api_interface.api.BuaBookApiQuerier;
import com.buabook.api_interface.enums.ERequestMethod;
import com.buabook.api_interface.enums.EMessageType;

@Component
@Scope("prototype")
public class GetBalance extends BuaBookApiQuerier {

	private static final String API_URL = "account/balance";
	
	
	public GetBalance() {
		super(EMessageType.GET_ACCOUNT_BALANCE_SUMMARY, ERequestMethod.GET, API_URL, null);
	}
	
	
	@Override
	protected void sendResponseToClient(JSONObject apiResponse) {
		JSONObject summaryObject = new JSONObject()
												.put("success", true)
												.put("response", apiResponse.getJSONObject("response").getJSONObject("summary"));
		
		super.sendResponseToClient(summaryObject);
	}
	
}
