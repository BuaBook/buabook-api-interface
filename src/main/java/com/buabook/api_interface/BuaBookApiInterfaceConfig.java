package com.buabook.api_interface;

import java.util.Map;

import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.buabook.api_interface.enums.EBrokerCommands;
import com.buabook.api_interface.enums.EOrderType;
import com.buabook.api_interface.helpers.ThreadPoolTaskRejectedHandler;
import com.buabook.api_interface.inbound.IJsonRequestValidator;
import com.buabook.api_interface.inbound.validators.RequestOrderValidator;
import com.buabook.api_interface.inbound.validators.CancelAllValidator;
import com.buabook.api_interface.inbound.validators.NewOrderValidator;
import com.buabook.api_interface.inbound.validators.OrderCancelValidator;
import com.buabook.http.common.HttpClient;
import com.buabook.spring.properties.PropertyLoader;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

@Configuration
@ComponentScan(basePackages= {"com.buabook.api_interface", "com.buabook.spring.configuration" })
@PropertySource(value="${app-properties-file}")
@EnableScheduling
public class BuaBookApiInterfaceConfig {
	
	private static final String DEFAULT_MARGIN_CONFIG = "buabook.order.default-margin";	
	
	private static final String DEAFULT_ORDER_TYPE_CONFIG = "buabook.order.default-order-type";
	
	
	@Value("${buabook.thread-pool.core-pool-size}")
	private Integer corePoolSize;
	
	@Value("${buabook.thread-pool.max-pool-size}")
	private Integer maxPoolSize;
	
	
	@Value("${buabook.validation.max-order-size}")
	private Integer maxOrderSize;
	
	@Value("${buabook.validation.max-order-price}")
	private Integer maxOrderPrice;
	
	
	@Autowired
	private PropertyLoader properties;
	
	
	@Bean
	public NewOrderValidator newOrderValidator() {
		return new NewOrderValidator(maxOrderSize, maxOrderPrice);
	}
	
	@Bean
	@DependsOn("newOrderValidator")
	public Map<EBrokerCommands, IJsonRequestValidator> brokerRequestValidators() {
		return ImmutableMap.<EBrokerCommands, IJsonRequestValidator>builder()
																		.put(EBrokerCommands.ORDER_NEW, newOrderValidator())
																		.put(EBrokerCommands.REQUEST_ABORT, new RequestOrderValidator())
																		.put(EBrokerCommands.ORDER_CANCEL, new OrderCancelValidator())
																		.put(EBrokerCommands.CANCEL_ALL, new CancelAllValidator())
																		.build();
	}

	@Bean
	public EOrderType defaultOrderTypeConfig() {
		String orderType = properties.getProperty(DEAFULT_ORDER_TYPE_CONFIG);
		
		if(Strings.isNullOrEmpty(orderType))
			return null;
		
		return EOrderType.valueOf(orderType);
	}
	
	@Bean
	public Double defaultOrderMarginConfig() {
		String marginConfig = properties.getProperty(DEFAULT_MARGIN_CONFIG);
		
		if(Strings.isNullOrEmpty(marginConfig))
			return null;
		
		return Double.valueOf(marginConfig);
	}
	
	@Bean
	public ThreadPoolTaskExecutor bbApiThreadPoolExecutor() {
		ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
		pool.setCorePoolSize(corePoolSize);
		pool.setMaxPoolSize(maxPoolSize);
		pool.setAwaitTerminationSeconds(5);
		pool.setThreadNamePrefix("API-ThrP-");
		pool.setRejectedExecutionHandler(new ThreadPoolTaskRejectedHandler());
		return pool;
	}
	
	@Bean
	public QueuedThreadPool bbWebSocketApiThreadPool() throws Exception {
		QueuedThreadPool threadPool = new QueuedThreadPool(4);
		threadPool.setName("API-WS-ThrP");
		threadPool.start();
		
		return threadPool;
	}
	
	@Bean
	public TaskScheduler bbTaskScheduler() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setThreadNamePrefix("API-Schd-ThrP-");
		taskScheduler.setAwaitTerminationSeconds(1);
		taskScheduler.setRejectedExecutionHandler(new ThreadPoolTaskRejectedHandler());
		
		return taskScheduler;
	}
	
	@Bean
	public HttpClient httpClient() {
		return new HttpClient();
	}
	
}

