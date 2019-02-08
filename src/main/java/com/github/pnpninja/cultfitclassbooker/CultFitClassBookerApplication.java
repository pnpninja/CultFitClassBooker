package com.github.pnpninja.cultfitclassbooker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.github.pnpninja.cultfitclassbooker.config.ConfigUtils;

@SpringBootApplication
@EnableScheduling
public class CultFitClassBookerApplication {

	private static final Logger logger = LogManager.getLogger(CultFitClassBookerApplication.class);
	
	public static void main(String[] args) {
		SpringApplication.run(CultFitClassBookerApplication.class, args);
		logger.info(ConfigUtils.getKey());
		
	}

}

