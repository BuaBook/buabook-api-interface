package com.buabook.api_interface.outbound;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.buabook.api_interface.enums.EBrokerCommands;
import com.buabook.api_interface.enums.EOrderType;
import com.buabook.api_interface.enums.ERequestMethod;

public class NewOrderOutboundRequest extends ApiOutboundRequest {
	private static final String API_URL = "orders/new";
	
	private static final double MIN_SUPPORTED_MARGIN = 0.2;
	
	private static final Double DEFAULT_DEFAULT_MARGIN = new Double(1d);
	
	private static final EOrderType DEFAULT_DEFAULT_ORDER_TYPE = EOrderType.MARKET;
	
	
	private final Double defaultMargin;
	
	private final EOrderType defaultOrderType;
	
	
	public NewOrderOutboundRequest(Double defaultMargin, EOrderType defaultOrderType) throws IllegalArgumentException {
		super(EBrokerCommands.ORDER_NEW, ERequestMethod.POST, API_URL);
		
		if(defaultMargin != null)
			if(defaultMargin.doubleValue() < MIN_SUPPORTED_MARGIN)
				throw new IllegalArgumentException("Configured default margin is less than permitted (" + MIN_SUPPORTED_MARGIN + ")");

		if(defaultMargin == null)
			defaultMargin = DEFAULT_DEFAULT_MARGIN;
		
		if(defaultOrderType == null)
			defaultOrderType = DEFAULT_DEFAULT_ORDER_TYPE;
			
		this.defaultMargin = defaultMargin;
		this.defaultOrderType = defaultOrderType;
	}

	@Override
	public void addToCommandArguments(JSONObject object) {
		if(object.has("game_orders")) {
			List<Object> newGameOrders = object.getJSONArray("game_orders").toList();
			List<Object> gameOrders = null;
			
			if(commandArguments.has("game_orders")) {
				gameOrders = commandArguments.getJSONArray("game_orders").toList();
				gameOrders.addAll(newGameOrders);
			} else {
				gameOrders = newGameOrders;
			}
			
			commandArguments.put("game_orders", gameOrders);
		}
		
		if(object.has("season_orders")) {
			List<Object> newSeasonOrders = object.getJSONArray("season_orders").toList();
			List<Object> seasonOrders = null;
			
			if(commandArguments.has("season_orders")) {
				seasonOrders = commandArguments.getJSONArray("season_orders").toList(); 
				seasonOrders.addAll(newSeasonOrders);
			} else {
				seasonOrders = newSeasonOrders;
			}
			
			commandArguments.put("season_orders", seasonOrders);
		}
	}
	
	@Override
	public JSONObject getCommandArguments() {
		super.getCommandArguments();
		addDefaultValues();
		
		return commandArguments;
	}
	
	private void addDefaultValues() {
		if(commandArguments.has("season_orders"))
			addDefaultValuesToOrders(commandArguments.getJSONArray("season_orders"));
		
		if(commandArguments.has("game_orders"))
			addDefaultValuesToOrders(commandArguments.getJSONArray("game_orders"));
			
	}
	
	private JSONArray addDefaultValuesToOrders(JSONArray productTypeOrders) {
		if(productTypeOrders == null || productTypeOrders.length() == 0)
			return productTypeOrders;
		
		productTypeOrders.forEach(orderObj -> {
			JSONObject order = (JSONObject) orderObj;
			
			if(! order.has("type"))
				order.put("type", defaultOrderType.toString());
			
			if(! order.has("margin_percent"))
				order.put("margin_percent", defaultMargin);
		});
		
		return productTypeOrders;
	}
	
}
