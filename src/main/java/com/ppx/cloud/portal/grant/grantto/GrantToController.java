package com.ppx.cloud.portal.grant.grantto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ppx.cloud.common.ControllerReturn;
import com.ppx.cloud.common.jdbc.Page;
import com.ppx.cloud.portal.grant.resource.ResourceService;
import com.ppx.cloud.portal.grant.service.ServiceService;


@Controller
public class GrantToController {
	
	@Autowired
	private GrantToService serv;
	
	@Autowired
	private ServiceService serviceService;
	
	@Autowired
	private ResourceService resourceService;
	
	@RequestMapping(path="grant/grantToRole")
    public ModelAndView grantToRole() {				
		ModelAndView mv = new ModelAndView("portal/grant/grantto/grantToRole");
		mv.addObject("listJson", listRole(new Page(), new RoleBean()));
		mv.addObject("listService", serviceService.listService());
		return mv;
	}
	
	@RequestMapping(path="grant/listRole") @ResponseBody
	public Map<String, Object> listRole(Page page, RoleBean role) {
		List<RoleBean> list = serv.listRole(page, role);
		return ControllerReturn.ok(list, page);
	}
	
	@SuppressWarnings("rawtypes")
	@RequestMapping(path="grant/getAuthorize") @ResponseBody
	public Map<String, Object> getAuthorize(@RequestParam Integer roleId, @RequestParam Integer serviceId) {
		Map<?, ?> authorizeMap = serv.getAuthorize(roleId, serviceId);
		if (authorizeMap == null) authorizeMap = new HashMap();
 		
		Map<?, ?> resMap = resourceService.getResource(serviceId);
		if (resMap == null) {
			return ControllerReturn.ok(-1);
		}
		return ControllerReturn.ok(authorizeMap, resMap);
	}
	
	@RequestMapping(path="grant/saveAuthorize") @ResponseBody
	public Map<String, Object> saveAuthorize(@RequestParam Integer roleId,
			@RequestParam Integer serviceId, @RequestParam String resIds) {
		serv.saveAuthorize(roleId, serviceId, resIds);
		return ControllerReturn.ok();
	}
		
	
}