package com.buabook.api_interface.outbound.test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.buabook.api_interface.enums.EBrokerCommands;
import com.buabook.api_interface.enums.EOrderType;
import com.buabook.api_interface.inbound.InboundRequest;
import com.buabook.api_interface.outbound.NewOrderOutboundRequest;


public class NewOrderOutboundRequestTest {
	private static final Double TEST_MARGIN_PERCENT = 0.6;
	
	private static final EOrderType TEST_ORDER_TYPE = EOrderType.MARKET;
	

	private NewOrderOutboundRequest newOrderRequest;
	
	
	@Before
	public void initialise() {
		this.newOrderRequest = new NewOrderOutboundRequest(TEST_MARGIN_PERCENT, TEST_ORDER_TYPE);
	}
	
	
	// NewOrderOutboundRequest.getCommandArguments

	@Test
	public void testGetCommandArgumentsReturnsEmptyArrayIfNoOrders() {
		JSONObject emptyOrders = new JSONObject()
											.put("season_orders", new JSONArray())
											.put("game_orders", new JSONArray());
		
		newOrderRequest.addToRequest(getInboundRequest(emptyOrders));
		
		JSONObject commandArguments = newOrderRequest.getCommandArguments();
		
		assertThat(commandArguments, is(not(nullValue())));
		assertThat(commandArguments.getJSONArray("season_orders").length(), is(equalTo(0)));
		assertThat(commandArguments.getJSONArray("game_orders").length(), is(equalTo(0)));
	}
	
	@Test
	public void testGetCommandArgumentsReturnsUnmodifiedObjectIfDefaultArgumentsAlreadyPresent() {
		JSONObject order = getGameOrder()
									.put("type", "TEST_TYPE")
									.put("margin_percent", 10.12);
		
		JSONObject orders = new JSONObject()
										.put("game_orders", new JSONArray().put(order));
		
		newOrderRequest.addToRequest(getInboundRequest(orders));
		
		JSONObject orderForRequest = newOrderRequest.getCommandArguments().getJSONArray("game_orders").getJSONObject(0);
		
		assertThat(orderForRequest.getString("type"), is(equalTo("TEST_TYPE")));
		assertThat(orderForRequest.getDouble("margin_percent"), is(equalTo(10.12)));
	}
	
	@Test
	public void testGetCommandArgumentsReturnsObjectWithDefaultArgumentsIfBothNotPresent() {
		JSONObject orders = new JSONObject()
									.put("game_orders", new JSONArray().put(getGameOrder()))
									.put("season_orders", new JSONArray().put(getGameOrder()));
		
		newOrderRequest.addToRequest(getInboundRequest(orders));
		
		JSONObject gameOrderForRequest = newOrderRequest.getCommandArguments().getJSONArray("game_orders").getJSONObject(0);
		
		assertThat(gameOrderForRequest.getString("type"), is(equalTo(TEST_ORDER_TYPE.toString())));
		assertThat(gameOrderForRequest.getDouble("margin_percent"), is(equalTo(TEST_MARGIN_PERCENT)));
		
		JSONObject seasonOrderForRequest = newOrderRequest.getCommandArguments().getJSONArray("game_orders").getJSONObject(0);
		
		assertThat(seasonOrderForRequest.getString("type"), is(equalTo(TEST_ORDER_TYPE.toString())));
		assertThat(seasonOrderForRequest.getDouble("margin_percent"), is(equalTo(TEST_MARGIN_PERCENT)));
	}
	
	@Test
	public void testGetCommandArgumentsReturnsObjectWithDefaultArgumentsIfTypeProvided() {
		JSONObject order = getGameOrder()
									.put("type", "TEST_TYPE");
		
		JSONObject orders = new JSONObject()
									.put("game_orders", new JSONArray().put(order));
		
		newOrderRequest.addToRequest(getInboundRequest(orders));
		
		JSONObject orderForRequest = newOrderRequest.getCommandArguments().getJSONArray("game_orders").getJSONObject(0);
		
		assertThat(orderForRequest.getString("type"), is(equalTo("TEST_TYPE")));
		assertThat(orderForRequest.getDouble("margin_percent"), is(equalTo(TEST_MARGIN_PERCENT)));
	}
	
	@Test
	public void testGetCommandArgumentsReturnsObjectWithDefaultArgumentsIfMarginPercentProvided() {
		JSONObject order = getGameOrder()
									.put("margin_percent", 10.11);
		
		JSONObject orders = new JSONObject()
									.put("game_orders", new JSONArray().put(order));
		
		newOrderRequest.addToRequest(getInboundRequest(orders));
		
		JSONObject orderForRequest = newOrderRequest.getCommandArguments().getJSONArray("game_orders").getJSONObject(0);
		
		assertThat(orderForRequest.getString("type"), is(equalTo(TEST_ORDER_TYPE.toString())));
		assertThat(orderForRequest.getDouble("margin_percent"), is(equalTo(10.11)));
	}
	
	
	private InboundRequest getInboundRequest(JSONObject argumentsJson) {
		return new InboundRequest(EBrokerCommands.ORDER_NEW, 0, UUID.randomUUID(), argumentsJson);
	}
	
	private JSONObject getGameOrder() {
		return new JSONObject()
							.put("player_id", 1)
							.put("price", 123)
							.put("size", 100)
							.put("side", "BUY")
							.put("game_id", 123456);
	}
	

}
