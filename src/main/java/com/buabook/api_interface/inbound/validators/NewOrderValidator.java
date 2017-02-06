package com.buabook.api_interface.inbound.validators;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.buabook.api_interface.client.RequestErrorException;
import com.buabook.api_interface.enums.EBrokerError;
import com.buabook.api_interface.enums.EProductType;
import com.buabook.api_interface.inbound.IJsonRequestValidator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class NewOrderValidator implements IJsonRequestValidator {
	private static final Logger log = Logger.getLogger(NewOrderValidator.class);
	

	private static final Integer DEFAULT_MAX_ORDER_SIZE = 200;
	
	private static final Integer DEFAULT_MAX_ORDER_PRICE = 1000;
	
	private static final Map<EProductType, List<String>> REQUIRED_ORDER_ARGS = ImmutableMap.<EProductType, List<String>>builder()
																															.put(EProductType.SEASON, ImmutableList.of("player_id", "price", "size", "side"))
																															.put(EProductType.GAME, ImmutableList.of("player_id", "price", "size", "side", "game_id"))
																															.build();
	
	
	private final Integer maxOrderSize;
	
	private final Integer maxOrderPrice; 
	
	
	
	public NewOrderValidator() {
		this(DEFAULT_MAX_ORDER_SIZE, DEFAULT_MAX_ORDER_PRICE);
	}

	public NewOrderValidator(Integer maxOrderSize, Integer maxOrderPrice) throws IllegalArgumentException {
		if(maxOrderSize == null || maxOrderSize <= 0)
			throw new IllegalArgumentException("Invalid max order size for new order validation");
		
		if(maxOrderPrice == null || maxOrderPrice <= 0)
			throw new IllegalArgumentException("Invalid max order price for new order validation");
		
		this.maxOrderSize = maxOrderSize;
		this.maxOrderPrice = maxOrderPrice;
	}


	@Override
	public JSONObject validateForCommand(JSONObject toValidate) throws RequestErrorException {
		JSONObject toReturn = null;
		
		try {
			toReturn = validateOrders(toValidate);
		} catch (JSONException e){
			log.warn("Failed to check new order sizes / prices. Error - " + e.getMessage(), e);
			throw new RequestErrorException(EBrokerError.BADLY_FORMED_JSON);
		}
		
		return toReturn;
	}
	
	/** No required arguments for new orders (although must have one or <code>game_orders</code> or <code>season_orders</code>) */
	@Override
	public List<String> getRequiredArgumentKeys() {
		return ImmutableList.of();
	}
	
	
	private JSONObject validateOrders(JSONObject arguments) throws RequestErrorException {
		boolean modified = false;
		
		int validSeasonOrdersSize = 0;
		int validGameOrdersSize = 0;
		
		if(arguments.has("season_orders")) {
			JSONArray seasonOrders = arguments.getJSONArray("season_orders");
			
			JSONArray validSeasonOrders = validateOrderArguments(EProductType.SEASON, seasonOrders);
			
			validSeasonOrdersSize = validSeasonOrders.length();
			modified = seasonOrders.length() != validSeasonOrdersSize;
			
			if(modified)
				arguments.put("season_orders", validSeasonOrders);
		}
		
		if(arguments.has("game_orders")) {
			JSONArray gameOrders = arguments.getJSONArray("game_orders");
			
			JSONArray validGameOrders = validateOrderArguments(EProductType.GAME, gameOrders);
			
			validGameOrdersSize = validGameOrders.length();
			modified = modified || gameOrders.length() != validGameOrdersSize; 
			
			if(modified)
				arguments.put("game_orders", validGameOrders);
		}
		
		if(validSeasonOrdersSize == 0 && validGameOrdersSize == 0)
			throw new RequestErrorException(EBrokerError.NO_ORDERS);
		
		if(! modified)
			return null;
		
		return arguments;
	}
	
	private JSONArray validateOrderArguments(EProductType productType, JSONArray orders) throws RequestErrorException {
		if(orders == null)
			return null;
		
		JSONArray filtered = new JSONArray();
		
		for(Object orderObj : orders) {
			if(!(orderObj instanceof JSONObject))
				throw new RequestErrorException(EBrokerError.BAD_ARGUMENTS);
			
			JSONObject order = (JSONObject) orderObj;
			
			if(! order.keySet().containsAll(REQUIRED_ORDER_ARGS.get(productType)))
				throw new RequestErrorException(EBrokerError.BAD_ARGUMENTS);
			
			if(order.getInt("size") > maxOrderSize || order.getInt("price") > maxOrderPrice)
				continue;
			
			filtered.put(order);
		}
		
		return filtered;
	}

}
