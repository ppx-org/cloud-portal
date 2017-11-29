package com.ppx.cloud.portal.grant.resource;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ppx.cloud.common.ControllerReturn;
import com.ppx.cloud.portal.grant.service.ServiceService;


@Controller
public class ResourceController {
	
	@Autowired
	private ResourceService resourceService;
	
	@Autowired
	private ServiceService serviceService;
	
	@RequestMapping(path="grant/resource")
    public ModelAndView resource() {				
		ModelAndView mv = new ModelAndView("portal/grant/resource/resource");
		mv.addObject("listService", serviceService.listService());
		return mv;
	}
	
	@RequestMapping(path="grant/getResource") @ResponseBody
	public Map<String, Object> getResource(@RequestParam Integer serviceId) {
		Map<?, ?> map = resourceService.getResource(serviceId);
		if (map == null) {
			return ControllerReturn.ok(-1);
		}
		return ControllerReturn.ok(map);
	}
	
	@RequestMapping(path="grant/saveResource") @ResponseBody
	public Map<String, Object> saveResource(@RequestParam Integer serviceId, @RequestParam String tree, String removeIds) {
		resourceService.saveResource(serviceId, tree, removeIds);
		return ControllerReturn.ok();
	}
		
	@RequestMapping(path="grant/getUri") @ResponseBody
	public Map<String, Object> getUri(@RequestParam String resId) {
		Map<?, ?> map = resourceService.getUri(resId);		
		if (map == null) {
			return ControllerReturn.ok(-1);
		}		
		return ControllerReturn.ok(map);
	}
	
	@RequestMapping(path="grant/saveUri") @ResponseBody
	public Map<String, Object> saveUri(@RequestParam String resId, @RequestParam String uri, String menuId) {
		Map<?, ?> map = resourceService.saveUri(resId, uri, menuId);
		return ControllerReturn.ok(map);
	}	
	
	@RequestMapping(path="grant/removeUri") @ResponseBody
	public Map<String, Object> removeUri(@RequestParam String resId, @RequestParam String uri, @RequestParam int uriIndex) {
		resourceService.removeUri(resId, uri, uriIndex);
		return ControllerReturn.ok();
	}
	
	
	
	
}