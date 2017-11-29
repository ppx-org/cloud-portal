package com.ppx.cloud.portal.grant.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ppx.cloud.common.ControllerReturn;


@Controller
public class ServiceController {
	
	@Autowired
	private ServiceService serv;
	
	@RequestMapping(path="grant/service")
    public ModelAndView service() {
		ModelAndView mv = new ModelAndView("portal/grant/service/service");		
		Map<String, Object> map = listService();	
		mv.addObject("listJson", map);
		return mv;
	}
	
	@RequestMapping(path="grant/listService") @ResponseBody
	public Map<String, Object> listService() {
		@SuppressWarnings("rawtypes")
		List<Map> list = serv.listService();
		return ControllerReturn.ok(list);
	}
	
	@RequestMapping(path="grant/addService") @ResponseBody
	public Map<String, Object> addService(@RequestParam String serviceName ,@RequestParam String contextPath) {	
		serv.addService(serviceName, contextPath);
		return ControllerReturn.ok();
	}
		
	@RequestMapping(path="grant/updateService") @ResponseBody
	public Map<String, Object> updateService(@RequestParam Integer _id,
			@RequestParam String serviceName,@RequestParam String contextPath) {
		serv.updateService(_id, serviceName, contextPath);
		return ControllerReturn.ok();
	}
	
	@RequestMapping(path="grant/removeService") @ResponseBody
	public Map<String, Object> removeService(@RequestParam Integer _id) {
		int r = serv.removeService(_id);
		return ControllerReturn.ok(r);
	}
		
	@RequestMapping(path="grant/orderService") @ResponseBody
	public Map<String, Object> orderService(@RequestParam String serviceIds) {
		serv.orderService(serviceIds);
		@SuppressWarnings("rawtypes")
		List<Map> list = serv.listService();		
		return ControllerReturn.ok(list);
	}
	
	
}