package com.buabook.api_interface.helpers;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;

public class ThreadPoolTaskRejectedHandler implements RejectedExecutionHandler {
	private static final Logger log = Logger.getLogger(ThreadPoolTaskRejectedHandler.class);

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		log.error("Thread pool task was rejected! [ Executor: " + executor.getThreadFactory() + " ] [ Runnable: " + r.getClass().getSimpleName() + " ]");
	}

}
