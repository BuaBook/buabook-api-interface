package com.buabook.api_interface.session;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.buabook.api_interface.BuaBookWebSocketManager;
import com.buabook.api_interface.api.BuaBookApiAccess;
import com.buabook.http.common.HttpClient;
import com.buabook.http.common.exceptions.HttpClientRequestFailedException;
import com.google.api.client.http.HttpResponse;
import com.google.common.base.Strings;
import com.google.common.net.MediaType;

@Component
public class Login implements Runnable {
	private static final Logger log = Logger.getLogger(Login.class);
	
	private static final Long LOGIN_REATTEMPT_WAIT_MS = 2000l;
	
	
	@Autowired
	private BuaBookApiAccess apiAccess;
	
	@Autowired
	private BuaBookWebSocketManager buabookWebSocket;
	
	@Autowired
	private HttpClient httpClient;
	
	
    @Value("${buabook.user-name}")
    private String userName;
    
    @Value("${buabook.password}")
    private String password;
    
    
    private JSONObject loginParams;
        
    
    @PostConstruct
    public void initialise() {
    	if(Strings.isNullOrEmpty(userName) || Strings.isNullOrEmpty(password))
    		throw new IllegalArgumentException("Missing username / password configuration");
    	
    	this.loginParams = new JSONObject()
    								.put("username", userName)
    								.put("password", password);
    }
    
    
    @Override
	public void run() {
    	String url = apiAccess.getApiUrl() + "userAccount/login";

    	JSONObject responseJson = null;
    	
    	log.info("Attemping login [ URL: " + url + " ] [ Username: " + loginParams.getString("username") + " ]");
    	
    	try {
    		HttpResponse response = httpClient.doPost(url, MediaType.JSON_UTF_8, loginParams.toString(), null); 
    		responseJson = HttpClient.getResponseAsJson(response);
    	} catch(HttpClientRequestFailedException | JSONException e) {
    		log.error("Failed to login due to exception. Error - " + e.getMessage());
    		reattemptLogin();
    		return;
    	}
    	
    	if((! responseJson.has("success")) || ! responseJson.getBoolean("success")) {
			log.error("Login failed due to API error. Response: " + responseJson.toString());
			reattemptLogin();
			return;
		}
    	
    	Integer userId = responseJson.getJSONObject("response").getInt("user_id");
		String token = responseJson.getJSONObject("response").getString("token");
		
		apiAccess.setUserId(userId);
		apiAccess.setToken(token);
		
		log.info("Login successful [ User ID: " + userId + " ] [ Token: " + token + " ]");
		
		try {
			buabookWebSocket.openApiSocket();
		} catch (Exception e) {
			log.fatal("Could not start WebSocket to BuaBook API! Application will EXIT. Error - " + e.getMessage(), e);
			System.exit(1);
		}
    }
    
    private void reattemptLogin() {
    	try {
			Thread.sleep(LOGIN_REATTEMPT_WAIT_MS);
		} catch (InterruptedException e) {}
    	
    	run();
    }
		
}
