package com.buabook.api_interface.api;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.buabook.api_interface.client.Broadcaster;
import com.buabook.api_interface.enums.EBrokerResponse;
import com.buabook.api_interface.enums.ERequestMethod;
import com.buabook.api_interface.helpers.ResponseBuilder;
import com.buabook.api_interface.outbound.ApiOutboundRequest;
import com.buabook.http.common.HttpClient;
import com.buabook.http.common.HttpHelpers;
import com.buabook.http.common.exceptions.HttpClientRequestFailedException;
import com.google.api.client.http.HttpResponse;
import com.google.common.base.Stopwatch;
import com.google.common.net.MediaType;

public class BuaBookApiRequest implements Runnable {
	private static final Logger log = Logger.getLogger(BuaBookApiRequest.class);
	
	
	private final HttpClient httpClient;
	
	private final BuaBookApiAccess apiAccess;
	
	private final ApiOutboundRequest outboundRequest;
	
	
	protected BuaBookApiRequest(HttpClient httpClient, BuaBookApiAccess apiAccess, ApiOutboundRequest request) {
		this.httpClient = httpClient;
		
		this.apiAccess = apiAccess;
		this.outboundRequest = request;
	}

	@Override
	public void run() {
		ERequestMethod requestMethod = outboundRequest.getRequestMethod();
		
		String baseUrl = apiAccess.getApiUrl() + outboundRequest.getRequestUrl();
		
		log.info("Sending API request [ Method: " + requestMethod + " ] [ URL: " + baseUrl + " ]");
		
		JSONObject acceptedJson = ResponseBuilder.build(outboundRequest.getCommand(), outboundRequest.getRequestIds(), outboundRequest.getClientIds(), null, EBrokerResponse.REQUEST_SENT);
		Broadcaster.getInstance().broadcast(acceptedJson);
		
		JSONObject responseJson = null;
		Stopwatch timer = Stopwatch.createStarted();
		
		try {
			HttpResponse response = null;
			
			if(requestMethod == ERequestMethod.GET) {
				String url = HttpHelpers.appendUrlParameters(baseUrl, apiAccess.getCredentialsAsMap());
				response = httpClient.doGet(url);
			}
			
			if(requestMethod == ERequestMethod.POST) {
				JSONObject postBody = outboundRequest.getCommandArguments();
				
				apiAccess.getCredentialsAsMap().forEach(postBody::put);
				
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

		if(! responseJson.getBoolean("success")) 
			log.warn("API response was error: " + responseJson.toString());
		
		JSONObject clientResponse = outboundRequest.getApiResponseForClient(responseJson);
		
		Broadcaster.getInstance().broadcast(clientResponse);
	}
	
}
