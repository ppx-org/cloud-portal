package com.ppx.cloud.portal.grant.port;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ppx.cloud.common.ControllerReturn;
import com.ppx.cloud.portal.cache.CacheConfiguration;
import com.ppx.cloud.portal.filter.FilterService;


@Controller
public class PortController {
	@Autowired
	private FilterService filterService;
	
	@RequestMapping(path="grant/cleanUserRoleCache")
	@ResponseBody
	public Map<String, Object> cleanUserRoleCache() {
		filterService.clearCache(CacheConfiguration.USER_ROLE_CACHE);
		return ControllerReturn.ok();
	}
	
	
	
}