package com.skcc.acs;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.jms.core.JmsTemplate;


@Configuration
@EnableJms
public class JmsConfig implements JmsListenerConfigurer {
	private static Logger logger = LoggerFactory.getLogger(JmsConfig.class);

	@Value("${jms.activemq.broker.url}")
	String brokerUrl;
	
	@Value("${jms.activemq.broker.username}")
	String userName;
	
	@Value("${jms.activemq.broker.password}")
	String password;
	
	@Value("${gate.opendoor.cctv.ids}")
	String cctvIds;
	
	@Autowired
	private SerialControllerManager serialControllerManager;
	
	@Bean
	public ActiveMQConnectionFactory connectionFactory(){
	    ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
	    connectionFactory.setBrokerURL(brokerUrl);
	    connectionFactory.setUserName(userName);
		connectionFactory.setPassword(password);
		
	    return connectionFactory;
	}

	@Bean
	public JmsTemplate jmsTemplate(){
	    JmsTemplate template = new JmsTemplate();
	    template.setConnectionFactory(connectionFactory());
	    //
	    template.setPubSubDomain(true);
	    return template;
	}	

	@Bean
	public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
	    DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
	    factory.setConnectionFactory(connectionFactory());
	    // core poll size=1 threads and max poll size 1 threads
	    factory.setConcurrency("1-1");
	    //
	    factory.setPubSubDomain(true);
	    return factory;
	}

	@Override
	public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {
		if (this.cctvIds == null) {
			logger.error("CCTV ids information is missing");
			return;
		}
		
		// ; 를 기준으로 cctvIds 파싱
		//this.cctvIds = "1221;1195";
		String cctvIdList[] = this.cctvIds.split(";");
		
		for (String cctvId : cctvIdList) {
			logger.info("Jms configuration for {}", cctvId);
			
			SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
	        endpoint.setId(cctvId);
	        endpoint.setDestination(cctvId);
	        /*
	        endpoint.setMessageListener(message -> {
	            // processing
	        	System.out.println(message);
	        });
	        */
	        endpoint.setMessageListener(new SubscribeProcessor(cctvId, serialControllerManager));
	        registrar.registerEndpoint(endpoint);
		}
	}
	
	

}