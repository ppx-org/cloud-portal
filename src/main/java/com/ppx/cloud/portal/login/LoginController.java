package com.ppx.cloud.portal.login;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.ppx.cloud.common.util.CookieUtils;
import com.ppx.cloud.portal.grant.PortalGrantUtils;

@Controller
public class LoginController {
	
	@Autowired
	private LoginService loginService;
	
	@GetMapping(path="login")
    public ModelAndView login() {
		ModelAndView mv = new ModelAndView("/portal/login/portalLogin");
		return mv;
	}
	
	@PostMapping(path="login/doLogin") @ResponseBody
	public Map<String, Object> doLogin(HttpServletRequest request, HttpServletResponse response, 
			@RequestParam String a, @RequestParam String p, @RequestParam String v) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> userMap = loginService.getUser(a, p);
		if (userMap == null) {
			// 帐号或密码错误
			returnMap.put("code", "-1");
			return returnMap;
		}
				
		// 帐号和密码正常，则在cookie上生成一个jwt token
		try {
		    Algorithm algorithm = Algorithm.HMAC256(PortalGrantUtils.getJwtPassword());
		    String token = JWT.create().withIssuedAt(new Date())
		    		.withClaim("USER_ID", (Integer)userMap.get("USER_ID"))
		    		.withClaim("USER_ACCOUNT", userMap.get("USER_ACCOUNT").toString())
		    		.withClaim("USER_NAME", userMap.get("USER_ACCOUNT").toString())
		    		.sign(algorithm);
		    CookieUtils.setCookie(response, PortalGrantUtils.PPXTOKEN, token);	
		} catch (Exception e){
			e.printStackTrace();
			returnMap.put("code", "-2");
			return returnMap;
		}
		
		if ("admin".equals(a)) {
			// 跳转到权限配置页面
			returnMap.put("code", "1");
		}
		else {
			returnMap.put("code", "2");
		}
		return returnMap;
	}
}
