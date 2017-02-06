package com.buabook.api_interface.query;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.buabook.api_interface.api.BuaBookApiQuerier;
import com.buabook.api_interface.enums.ERequestMethod;
import com.buabook.api_interface.enums.EMessageType;

@Component
@Scope("prototype")
public class GetAllBooks extends BuaBookApiQuerier {
	
	private static final String API_URL = "players/book/all";
	
	
	public GetAllBooks() {
		super(EMessageType.PLAYERS_GET_BOOK_RESPONSE, ERequestMethod.GET, API_URL, null);
	}
	
}
