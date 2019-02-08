package com.github.pnpninja.cultfitclassbooker.config;

import java.util.HashMap;
import java.util.Map;

public final class CultFitConstants {
	
	public final class BookingState{
		public static final String BOOKED = "BOOKED";
		public static final String AVAILABLE = "AVAILABLE";
		public static final String UNAVAILABLE = "SEAT_NOT_AVAILABLE";
	}
	public static Map<Integer,String> DAY_MAP = new HashMap<Integer,String>();
	static {
		DAY_MAP.put(0, "SUNDAY");
		DAY_MAP.put(1, "MONDAY");
		DAY_MAP.put(2, "TUESDAY");
		DAY_MAP.put(3, "WEDNESDAY");
		DAY_MAP.put(4, "THURSDAY");
		DAY_MAP.put(5, "FRIDAY");
		DAY_MAP.put(6, "SATURDAY");
	}
	

}
