package com.buabook.api_interface.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.buabook.api_interface.RequestQueueController;
import com.buabook.api_interface.enums.EBrokerCommands;
import com.buabook.api_interface.outbound.ApiOutboundRequest;
import com.buabook.http.common.HttpClient;

@Component
public class BuaBookApiSender extends Thread {
	private static final Logger log = Logger.getLogger(BuaBookApiSender.class);
	
	private static final Long MIN_BATCH_INTERVAL_MS = 250l;
	

	@Autowired
	private HttpClient httpClient;

	@Autowired
    private ThreadPoolTaskExecutor taskExecutor;
	
	@Autowired
	private RequestQueueController requestQueue;
	
	@Autowired
	private BuaBookApiAccess apiAccess;
	
	
	@Value("${buabook.buabook-api.request-batch-ms}")
	private Long batchIntervalMs;
	
	
	private Boolean tradingEnabled;
	
	private Map<EBrokerCommands, Long> requestsSummary;
	
	
	@PostConstruct
	public final void initialise() {
		if(batchIntervalMs < MIN_BATCH_INTERVAL_MS)
			throw new IllegalArgumentException("Minimum batch interval must be at least " + MIN_BATCH_INTERVAL_MS + "ms");
	}
	
	@Override
	public synchronized void start() {
		log.info("Starting BuaBook API Interface thread [ BuaBook API: " + apiAccess.getApiUrl() + " ] [ Batch Interval: " + batchIntervalMs + " ms ]");
		
		this.tradingEnabled = true;
		this.requestsSummary = new HashMap<>();
		this.setName("BB-API-Interface");
		
		super.start();
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				Thread.sleep(batchIntervalMs);
			} catch (InterruptedException e) {}
			
			if(! apiAccess.accessAvailable()) {
				log.warn("API access is not available. Waiting for access...");
				continue;
			}
			
			Map<EBrokerCommands, ApiOutboundRequest> nextOutboundRequests = requestQueue.getNextOutboundRequests();
			
			if(nextOutboundRequests.isEmpty())
				continue;
			
			if(! tradingEnabled) {
				log.warn("TRADING DISABLED! The following client requests will be lost: " + nextOutboundRequests);
				continue;
			}
			
			nextOutboundRequests.forEach((key, val) -> requestsSummary.merge(key, 1l, Long::sum));
			
			nextOutboundRequests.values().stream()
											.filter(Objects::nonNull)
											.sorted((o1, o2) -> o1.getCommand().compareTo(o2.getCommand()))
											.forEach(request -> taskExecutor.execute(new BuaBookApiRequest(httpClient, apiAccess, request)));
		}
	}
	
	
	public Boolean isTradingEnabled() {
		return tradingEnabled;
	}
	
	public Map<EBrokerCommands, Long> getRequestsSummary() {
		return requestsSummary;
	}
	
	public void enableTrading() {
		log.info("Request to enable trading received");
		
		synchronized (tradingEnabled) {
			if(tradingEnabled)
				return;

			this.tradingEnabled = true;
		}
	}
	
	public void disableTrading() {
		log.info("Request to disable trading received");
		
		synchronized (tradingEnabled) {
			if(! tradingEnabled)
				return;
			
			this.tradingEnabled = false;
		}
	}
}
