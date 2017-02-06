package com.buabook.api_interface.query;

import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.buabook.api_interface.api.BuaBookApiQuerier;
import com.buabook.api_interface.enums.EProductType;
import com.buabook.api_interface.enums.ERequestMethod;
import com.buabook.api_interface.enums.EMessageType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

@Component
@Scope("prototype")
public class GetGameTradeSummary extends BuaBookApiQuerier {

	private static final Map<String, Object> API_PARAMS = ImmutableMap.<String, Object>builder()
																							.put("product_type", EProductType.GAME)
																							.put("lite", Boolean.TRUE)
																							.build();

	private static final String API_URL = "marketSummary/trade";
	
	
	public GetGameTradeSummary() {
		super(EMessageType.GET_TRADE_SUMMARY_RESPONSE, ERequestMethod.GET, API_URL, Maps.newHashMap(API_PARAMS));
	}

}
