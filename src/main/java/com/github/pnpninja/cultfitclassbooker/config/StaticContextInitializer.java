package com.github.pnpninja.cultfitclassbooker.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class StaticContextInitializer {

    @Autowired
    private CultFitConfig cultFitConfig;

    @Autowired
    private ApplicationContext context;

    @PostConstruct
    public void init() {
        ConfigUtils.setMyConfig(cultFitConfig);
    }
}
