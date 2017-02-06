package com.buabook.api_interface.api;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

@Component
@Scope("singleton")
public class BuaBookApiAccess {
	
    @Value("${buabook.buabook-api.url}")
    private String apiUrl;
    
    
	private Integer userId;
	
	private String token;
	
	
	private Map<String, Object> credentialsAsMap;

	
	public String getApiUrl() {
		return apiUrl;
	}
	
	public Integer getUserId() {
		return userId;
	}
	
	public String getUserIdAsString() {
		if(userId == null)
			return "";
		
		return userId.toString();
	}

	public String getToken() {
		return token;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	public boolean accessAvailable() {
		return (userId != null) && ! Strings.isNullOrEmpty(token);
	}
	
	public void invalidateAccess() {
		setUserId(null);
		setToken(null);
		
		this.credentialsAsMap = null;
	}
	
	public Map<String, Object> getCredentialsAsMap() {
		if(! accessAvailable())
			return null;
		
		if(credentialsAsMap == null)
			credentialsAsMap = ImmutableMap.<String, Object>builder()
																.put("user_id", userId)
																.put("token", token)
																.build();
		
		return credentialsAsMap;
	}
	
}
