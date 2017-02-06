package com.buabook.api_interface.session;

import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.buabook.api_interface.api.BuaBookApiAccess;
import com.buabook.http.common.HttpClient;
import com.buabook.http.common.HttpHelpers;
import com.buabook.http.common.exceptions.HttpClientRequestFailedException;

@Component
public class Keepalive {
	private static final Logger log = Logger.getLogger(Keepalive.class);
	
	private static final long KEEPALIVE_INTERVAL_MS = 2 * 60 * 1000l;
	
	
	@Autowired
	private HttpClient httpClient;
	
	@Autowired
	private BuaBookApiAccess apiAccess;
	
	@Autowired
	private Login login;
	
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;
	
    
    @Scheduled(initialDelay=KEEPALIVE_INTERVAL_MS, fixedDelay=KEEPALIVE_INTERVAL_MS)
    public void keepalive() {
    	if(! apiAccess.accessAvailable()) {
			log.warn("Cannot keep-alive as there is no active token. Re-login.");
			taskExecutor.execute(login);
			return;
		}
    	
    	Map<String, Object> credentials = apiAccess.getCredentialsAsMap();
		
		log.info("Attempting Keepalive [ Credentials: " + credentials + " ]");
		
		String url = HttpHelpers.appendUrlParameters(apiAccess.getApiUrl() + "keepalive/session", credentials);
		
		JSONObject responseJson = null;
		
		try {
			responseJson = HttpClient.getResponseAsJson(httpClient.doGet(url));
		} catch (HttpClientRequestFailedException | JSONException e) {
			keepaliveFailed(e.getMessage());
			return;
		}
		
		if((! responseJson.has("success")) || ! responseJson.getBoolean("success")) {
			keepaliveFailed(responseJson.toString());
			return;
		}
		
		log.info("Keepalive OK [ Credentials: " + credentials + " ]");
    }

    private void keepaliveFailed(String error) {
    	log.error("Failed to keepalive session. Re-attempting login. Error - " + error);
    	
    	apiAccess.invalidateAccess();
		taskExecutor.execute(login);
    }
}
