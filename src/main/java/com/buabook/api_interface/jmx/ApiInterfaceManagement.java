package com.buabook.api_interface.jmx;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.buabook.api_interface.api.BuaBookApiAccess;
import com.buabook.api_interface.api.BuaBookApiSender;
import com.buabook.api_interface.session.Login;
import com.buabook.spring.jmx.IExposeToJmx;
import com.buabook.spring.jmx.JmxResult;

@Component
@ManagedResource(
		objectName="com.buabook.api_interface:name=API Management Interface",
		description="Administration of core API interface features"
)
public class ApiInterfaceManagement implements IExposeToJmx {
	private static final Logger log = Logger.getLogger(ApiInterfaceManagement.class);
	

	@Autowired
	private BuaBookApiAccess apiAccess;
	
	@Autowired
	private BuaBookApiSender apiInterface;
	
	@Autowired
    private ThreadPoolTaskExecutor taskExecutor;
	
	@Autowired
	private Login login;
	
	
	@ManagedAttribute(description = "Shows current user ID and token in use")
	public Map<String, Object> getCurrentLoginValues() {
		return new HashMap<>(apiAccess.getCredentialsAsMap());
	}
	
	@ManagedAttribute(description = "Shows whether API access is currently permitted")
	public Boolean getIsApiAccessEnabled() {
		return apiInterface.isTradingEnabled();
	}
	
	@ManagedAttribute(description = "The number of active API interface threads")
	public Integer getCurrentApiInterfaceThreadCount() {
		return taskExecutor.getActiveCount();
	}
	
	@ManagedAttribute(description = "The total number of requests sent of each supported Interface command")
	public Map<String, Long> getApiRequestsSummary() {
		return apiInterface.getRequestsSummary().entrySet()
															.stream()
															.collect(
																	Collectors.toMap(	me -> me.getKey().toString(), 
																						me -> me.getValue())
															);
	}
	
	
	@ManagedOperation(description="Disable all API access")
	public String disableApiAccess() {
		apiInterface.disableTrading();
		
		return JmxResult.OK;
	}
	
	@ManagedOperation(description="Enable all API access")
	public String enableApiAccess() {
		apiInterface.enableTrading();
		
		return JmxResult.OK;
	}
	
	@ManagedOperation(description="Forces the interface to login again with the BuaBook API")
	public String forceApiLogin() {
		apiAccess.invalidateAccess();
		
		Future<?> loginResult = taskExecutor.submit(login);
		
		try {
			loginResult.get();
		} catch (InterruptedException | ExecutionException e) {
			log.error("Failed to re-login after manual request. Error - " + e.getMessage(), e);
			return JmxResult.ERROR;
		}
		
		return JmxResult.OK;
	}

}
