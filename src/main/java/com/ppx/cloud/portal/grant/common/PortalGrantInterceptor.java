package com.ppx.cloud.portal.grant.common;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ppx.cloud.common.ControllerReturn;
import com.ppx.cloud.common.PropertiesConfig;
import com.ppx.cloud.common.util.CookieUtils;
import com.ppx.cloud.portal.filter.StartListener;
import com.ppx.cloud.portal.grant.PortalGrantUtils;
import com.ppx.cloud.portal.log.LogService;

/**
 * 
 * @author dengxz
 *
 */

/**
 * 拦截器
 * @author dengxz
 * @date 2017年11月20日
 */
public class PortalGrantInterceptor implements HandlerInterceptor {

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		
		
		String forwardUri = (String)request.getAttribute("javax.servlet.forward.request_uri");
		if (forwardUri != null && forwardUri.endsWith("/login")) {
			return true;
		}
		if (request.getRequestURI().endsWith("/login") || request.getRequestURI().endsWith("/doLogin")) {
			return true;
		}
		
		
		
		// 从cookie中取得tocken
		Map<String, String> cookieMap = CookieUtils.getCookieMap(request);
		String token = cookieMap.get(PortalGrantUtils.PPXTOKEN);		

		// token为空,表示未登录
		if (StringUtils.isEmpty(token)) {
			return toLogin(request, response, "Not Login");
		}
		
		Integer userId = null;
		String userAccount = "";
		try {
			Algorithm algorithm = Algorithm.HMAC256(PortalGrantUtils.getJwtPassword());
			JWTVerifier verifier = JWT.require(algorithm).build();
			DecodedJWT jwt = verifier.verify(token);
			userId = jwt.getClaim("USER_ID").asInt();
			userAccount = jwt.getClaim("USER_ACCOUNT").asString();
			String userName = jwt.getClaim("USER_NAME").asString();
			
			LoginUser u = new LoginUser();
			u.setUserId(userId);
			u.setUserAccount(userAccount);
			u.setUserName(userName);
			PortalGrantContext.setLoginUser(u);
			
		} catch (Exception e) {
			e.printStackTrace();
			return toLogin(request, response, e.getMessage());
		}
		
		LogService logService = (LogService) StartListener.staticContext.getBean("logService");
		
		// 超级用户才能访问权限管理平台的权限
		if (!"admin".equals(userAccount)) {
			response(request, response, 403, "Unauthorized", userId);
			return false;
		}
		
		
		
		// 判断是否为错误页面
		Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
		if (statusCode != null) {
			// request.getRequestURI() 为/portal/error
			String requestUri = (String)request.getAttribute("javax.servlet.forward.request_uri");		
			Exception exception = (Exception)request.getAttribute("javax.servlet.error.exception");
			String msg = "";
			if (statusCode == 404) {
				msg = "Not Found";
			}
			else if (exception != null) {
				msg = exception.getMessage();
			}
			String accept = request.getHeader("accept");
			if (accept != null && accept.indexOf("text/html") >= 0) {
				// html返回
				ControllerReturn.returnErrorHtml(response, statusCode, msg);
			} else {
				// json返回
				ControllerReturn.returnErrorJson(response, statusCode, msg);
			}
			
			// 写入日志到mongodb			
			logService.saveExceptionAccessLog(request, requestUri, statusCode, msg, userId);
			return false;
		}
		else {
			logService.saveExceptionAccessLog(request, null, null, userId);
		}
		
		return true;
	}

	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
	}

	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
	}
	
	private boolean toLogin(HttpServletRequest request, HttpServletResponse response, String msg) {
		try {
			String accept = request.getHeader("accept");
			if (accept != null && accept.indexOf("text/html") >= 0) {
				// 跳转到登录页面
				response.sendRedirect(PropertiesConfig.contextPath + "/login");
			} else {
				// json返回 HTTP 401 - 未授权：登录失败
				ControllerReturn.returnErrorJson(response, 401, msg);
			}
			
			// 写入日志到mongodb
			LogService logService = (LogService) StartListener.staticContext.getBean("logService");
			logService.saveExceptionAccessLog(request, 401, msg, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private void response(HttpServletRequest request, HttpServletResponse response,  int code, String msg, int userId) {
		String accept = request.getHeader("accept");
		if (accept != null && accept.indexOf("text/html") >= 0) {
			ControllerReturn.returnErrorHtml(response, code, msg);
		} else {
			// json返回
			ControllerReturn.returnErrorJson(response, code, msg);
		}

		// 写入日志到mongodb
		LogService logService = (LogService) StartListener.staticContext.getBean("logService");
		logService.saveExceptionAccessLog(request, code, msg, userId);
	}
}