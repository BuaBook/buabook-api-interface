package com.buabook.api_interface.streaming;

import java.util.Map;
import java.util.function.Function;

import org.json.JSONArray;
import org.json.JSONObject;

import com.buabook.api_interface.enums.EBrokerResponse;
import com.buabook.api_interface.enums.EMessageType;
import com.buabook.api_interface.enums.EProductType;
import com.google.common.collect.ImmutableMap;

public final class StreamingUpdateConverters {
	private static final Map<EMessageType, Function<JSONObject, JSONObject>> converters = 
														ImmutableMap.<EMessageType, Function<JSONObject,JSONObject>>builder()
																															.put(EMessageType.PLAYERS_GET_BOOK_RESPONSE, StreamingUpdateConverters::reformatBookResponse)
																															.build();
																																								

	public static JSONObject convert(JSONObject original) {
		EMessageType updateType = EMessageType.fromMessageType(original.getString("messageType"));
		
		JSONObject apiResponse = original;
		
		if(converters.containsKey(updateType))
			apiResponse = converters.get(updateType).apply(original);
		
		return new JSONObject()
							.put("response_type", EBrokerResponse.UPDATE)
							.put("response", apiResponse);
	}
	
	
	/** Converts a individual player book update to look the same as the all books response that is sent as an initial update */
    private static JSONObject reformatBookResponse(JSONObject bookResponse) {
    	JSONObject oldResponseValue = bookResponse.getJSONObject("response");
    	
    	JSONArray gameBooks = new JSONArray();
    	JSONArray seasonBooks = new JSONArray();
    	
    	EProductType productType = oldResponseValue.getEnum(EProductType.class, "product_type");
    	oldResponseValue.remove("product_type");
    	
    	if(EProductType.GAME == productType)
    		gameBooks.put(oldResponseValue);
    	else if(EProductType.SEASON == productType)
    		seasonBooks.put(oldResponseValue);
    	
    	JSONObject newResponseValue = new JSONObject()
    											.put("game_books", gameBooks)
    											.put("season_books", seasonBooks);
    	
    	return new JSONObject()
							.put("response", newResponseValue)
							.put("messageType", EMessageType.PLAYERS_GET_BOOK_RESPONSE.asMessageType());
    }
}
