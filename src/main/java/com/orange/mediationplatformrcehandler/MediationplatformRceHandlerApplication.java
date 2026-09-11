package com.MyProject.mediationplatformrcehandler;

import org.dozer.DozerBeanMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = "com.MyProject")
@EnableMongoAuditing
@EnableTransactionManagement
@EnableAsync
@EnableScheduling
public class MediationplatformRceHandlerApplication {

	static {
		System.setProperty("org.apache.commons.logging.Log",
				"org.apache.commons.logging.impl.NoOpLog");
	}


	@Bean
	DozerBeanMapper dozerMapper() {
		return new DozerBeanMapper();
	}

	public static void main(String[] args) {
		SpringApplication.run(MediationplatformRceHandlerApplication.class, args);
	}
}
