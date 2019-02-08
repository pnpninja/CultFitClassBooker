package com.github.pnpninja.cultfitclassbooker.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import ch.qos.logback.core.joran.spi.DefaultClass;

@Configuration
class CultFitConfig {
	
	@Value("${cultfit.apiKey}")
	private String API_KEY;
	
	@Value("${cultfit.cookie}")
	private String API_COOKIE;
	
	//20 is Cult Mahadevpura, Bangalore
	@Value("${cultfit.centerId:20}")
	private String CENTER_ID;
	
	@Value("#{'${cultfit.allClasses}'.split(',')}")
	private List<String> ALL_CLASSES;
	
	@Value("#{'${cultfit.preferredClassesOrdered}'.split(',')}")
	private List<String> PREFERRED_CLASSES;
	
	@Value("#{'${cultfit.bookingDays}'.split(',')}")
	private List<String> BOOKING_DAYS;
	
	@Value("#{'${cultfit.timeSlotsOrdered}'.split(',')}")
	private List<String> TIME_SLOTS_ORDERED;
	
	

	public String getAPI_COOKIE() {
		return API_COOKIE;
	}


	public String getAPI_KEY() {
		return API_KEY;
	}


	public String getCENTER_ID() {
		return CENTER_ID;
	}


	public List<String> getALL_CLASSES() {
		return ALL_CLASSES;
	}


	public List<String> getPREFERRED_CLASSES() {
		return PREFERRED_CLASSES;
	}


	public List<String> getBOOKING_DAYS() {
		return BOOKING_DAYS;
	}


	public List<String> getTIME_SLOTS_ORDERED() {
		return TIME_SLOTS_ORDERED;
	}
	

}
