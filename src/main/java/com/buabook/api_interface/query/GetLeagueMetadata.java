package com.buabook.api_interface.query;

import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.buabook.api_interface.api.BuaBookApiQuerier;
import com.buabook.api_interface.enums.ERequestMethod;
import com.buabook.api_interface.enums.EMessageType;

@Component
@Scope("prototype")
public class GetLeagueMetadata extends BuaBookApiQuerier {
	
	private static final String API_URL = "metadata/league";
	
	
	public GetLeagueMetadata() {
		super(EMessageType.GAME, ERequestMethod.GET, API_URL, null);
	}
	
	@Override
	protected void sendResponseToClient(JSONObject apiResponse) {
		JSONObject gamesResponse = new JSONObject()
												.put("success", true)
												.put("response", apiResponse.getJSONObject("response").getJSONArray("games"));
		
		super.sendResponseToClient(gamesResponse);
	}
}
