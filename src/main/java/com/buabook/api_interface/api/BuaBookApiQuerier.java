package com.buabook.api_interface.api;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.buabook.api_interface.client.Broadcaster;
import com.buabook.api_interface.enums.EBrokerResponse;
import com.buabook.api_interface.enums.ERequestMethod;
import com.buabook.api_interface.enums.EMessageType;
import com.buabook.http.common.HttpClient;
import com.buabook.http.common.HttpHelpers;
import com.buabook.http.common.exceptions.HttpClientRequestFailedException;
import com.google.api.client.http.HttpResponse;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.MediaType;

public class BuaBookApiQuerier implements Runnable {
	private static final Logger log = Logger.getLogger(BuaBookApiQuerier.class);
	
	
	@Autowired
	private BuaBookApiAccess apiAccess;
	
	@Autowired
	private HttpClient httpClient;
	
	
	private EMessageType updateType;

	private ERequestMethod requestMethod;
	
	private Map<String, Object> requiredArgs;
	
	private String requestUrl;
	
	
	/**
	 * 
	 * @param updateType
	 * @param requestMethod
	 * @param requestUrl
	 * @param requiredArgs <b>NOTE</b>: This is a {@link HashMap} to prevent use of an immutable map type (e.g. as returned by {@link ImmutableMap#builder()})
	 * @throws IllegalArgumentException
	 */
	public BuaBookApiQuerier(EMessageType updateType, ERequestMethod requestMethod, String requestUrl, HashMap<String, Object> requiredArgs) throws IllegalArgumentException {
		if(requestMethod == null || Strings.isNullOrEmpty(requestUrl))
			throw new IllegalArgumentException("Missing update type / request method / request URL");
		
		this.updateType = updateType;
		this.requestMethod = requestMethod;
		this.requestUrl = requestUrl;
		this.requiredArgs = requiredArgs;
	}

	
	@Override
	public void run() {
		while(! apiAccess.accessAvailable()) {
			log.warn("API access is not currently available. Waiting for access before attempting to query API");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
		}
		
		if(requiredArgs == null)
			requiredArgs = apiAccess.getCredentialsAsMap();
		else
			requiredArgs.putAll(apiAccess.getCredentialsAsMap());
		
		JSONObject apiResponse = queryApi();
		sendResponseToClient(apiResponse);
	}
	
	protected JSONObject queryApi() {
		String baseUrl = apiAccess.getApiUrl() + requestUrl;
		
		log.info("Sending API request [ Method: " + requestMethod + " ] [ URL: " + baseUrl + " ] [ Arguments: " + requiredArgs + " ]");
		
		JSONObject responseJson = null;
		Stopwatch timer = Stopwatch.createStarted();
		
		try {
			HttpResponse response = null;
		
			if(requestMethod == ERequestMethod.GET) {
				String url = HttpHelpers.appendUrlParameters(baseUrl, requiredArgs);
				response = httpClient.doGet(url);
			} else if(requestMethod == ERequestMethod.POST) {
				JSONObject postBody = new JSONObject(requiredArgs);
				response = httpClient.doPost(baseUrl, MediaType.JSON_UTF_8, postBody.toString(), null);
			}
			
			responseJson = HttpClient.getResponseAsJson(response);
		} catch (HttpClientRequestFailedException e) {
			log.error("Failed to send request! Error - " + e.getMessage(), e);
			
			responseJson = new JSONObject()
										.put("success", false)
										.put("exception", e.getMessage());
		}
		
		log.info("API response received [ Method: " + requestMethod + " ] [ URL: " + baseUrl + " ] [ In Flight: " + timer.stop() + " ]");
		
		return responseJson;
	}
	
	protected void sendResponseToClient(JSONObject apiResponse) {
		if(updateType == null) {
			log.debug("Querier not configured to stream responses to client. JSON response: " + apiResponse);
			return;
		}
		
		EBrokerResponse responseType = EBrokerResponse.INITIAL_UPDATE;
		
		if(! apiResponse.getBoolean("success")) {
			log.warn("API response was error: " + apiResponse.toString());
			responseType = EBrokerResponse.UPDATE_ERROR;
		}
		
		apiResponse.put("messageType", updateType.asMessageType());
		
		JSONObject clientResponse = new JSONObject()
												.put("response_type", responseType)
												.put("response", apiResponse);
			
		Broadcaster.getInstance().broadcast(clientResponse);
	}

}
