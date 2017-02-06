package com.buabook.api_interface;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.buabook.api_interface.api.BuaBookApiSender;
import com.buabook.api_interface.session.Login;
import com.buabook.api_interface.sockets.ClientWebSocketHandler;

@SpringBootApplication
public class BuaBookApiInterface implements CommandLineRunner {
	private static final Logger log = Logger.getLogger(BuaBookApiInterface.class);
	
	
    @Autowired
    private ClientWebSocketHandler botSocketHandler;
    
    @Autowired
    private BuaBookApiSender apiSender;
    
    @Autowired
    private Login login;
    
    
    @Value("${buabook.jetty-server.threads}")
	private Integer jettyServerThreads;
    
    @Value("${buabook.jetty-server.listen-port}")
    private Integer jettyListenPort;
    
    
    public static void main( String[] args ) {
		SpringApplication application = new SpringApplication(BuaBookApiInterface.class);
		
		try {
			application.run(args);
		} catch (Exception e) {
			log.fatal("Application failed to initialise. Application will now EXIT!");
			System.exit(2);
		}
    }

	@Override
	public void run(String... arg0) throws Exception {
		login.run();
		apiSender.start();
		
		startJetty();
	}
	
	private void startJetty() throws Exception {
		QueuedThreadPool jettyThreadPool = new QueuedThreadPool(jettyServerThreads);
		Server server = new Server(jettyThreadPool);
	
		ServerConnector http = new ServerConnector(server, new HttpConnectionFactory());
		http.setPort(jettyListenPort);
		
		server.addConnector(http);
		
		ContextHandler contextHandler = new ContextHandler();
		contextHandler.setHandler(botSocketHandler);
		server.setHandler(contextHandler);
		
    	server.start();
    	server.join();
	}
}