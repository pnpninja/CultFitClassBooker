package com.github.pnpninja.cultfitclassbooker.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KeepAliveController {
	
	@RequestMapping(value="/")
	public int keepAlive() {
		return 200;
	}
	
	

}
