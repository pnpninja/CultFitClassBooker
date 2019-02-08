package com.github.pnpninja.cultfitclassbooker.config;

import java.util.List;

public final class ConfigUtils {

    private static CultFitConfig cultFitConfig;

    public static void setMyConfig(CultFitConfig cultFitConfig) {
        ConfigUtils.cultFitConfig = cultFitConfig;
    }

    private ConfigUtils() {
    }

    public static String getKey() {
      return cultFitConfig.getAPI_KEY();
    }
    
    public static String getCookie() {
    	return cultFitConfig.getAPI_COOKIE();
    }
    
    public static List<String> getBookingDays(){
    	return cultFitConfig.getBOOKING_DAYS();
    }
    
    public static List<String> getPreferredClasses(){
    	return cultFitConfig.getPREFERRED_CLASSES();
    }
    
    public static List<String> getPreferredTimings(){
    	return cultFitConfig.getTIME_SLOTS_ORDERED();
    }
}
