package com.skcc.acs;

import javax.annotation.PreDestroy;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VasAcsControllerApplication implements CommandLineRunner {
	//private static Logger logger = LoggerFactory.getLogger(VasAcsControllerApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(VasAcsControllerApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// TODO Auto-generated method stub
	}
	
	@PreDestroy
	public void onDestroy() throws Exception {
		
	}
}
