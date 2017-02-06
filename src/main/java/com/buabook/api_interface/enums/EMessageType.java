package com.buabook.api_interface.enums;

import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;

public enum EMessageType {
	
	GET_ACCOUNT_BALANCE_SUMMARY,
	
	GAME,
	
	NEWS_ITEM,
	FAST_NEWS_ITEM,
	
	ORDER_LIST_RESPONSE,
	GET_POSITIONS_RESPONSE,
	
	PLAYERS_GET_ALL_BOOKS_RESPONSE,
	PLAYERS_GET_BOOK_RESPONSE,
	
	GET_PLAYER_EXPECTATION_STATS_RESPONSE,
	
	GET_TRADE_SUMMARY_RESPONSE;
	
	
	public static EMessageType fromMessageType(String messageType) {
		if(Strings.isNullOrEmpty(messageType))
			return null;
		
		return EMessageType.valueOf(CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, messageType));
	}
	
	public String asMessageType() {
		return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name());
	}
}
