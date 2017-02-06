package com.buabook.api_interface.query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class InitialClientConnectionQuerier {
	
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;
    
    @Autowired
    private ApplicationContext context;
    
    
	public void queryApiOnClientConnect() {
		taskExecutor.execute(context.getBean(GetLeagueMetadata.class));
		
		taskExecutor.execute(context.getBean(GetSeasonTradeSummary.class));
		taskExecutor.execute(context.getBean(GetGameTradeSummary.class));
		taskExecutor.execute(context.getBean(GetAllBooks.class));
		
		taskExecutor.execute(context.getBean(GetActiveGameOrders.class));
		taskExecutor.execute(context.getBean(GetActiveSeasonOrders.class));
		
		taskExecutor.execute(context.getBean(GetActiveGamePositions.class));
		taskExecutor.execute(context.getBean(GetActiveSeasonPositions.class));
		
		taskExecutor.execute(context.getBean(GetBalance.class));
	}
}
