package com.ppx.cloud.common.util;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * cookie工具类
 * @author dengxz
 * @date 2017年11月14日
 */
public class CookieUtils {
	
	public static Map<String, String> getCookieMap(HttpServletRequest request) {		
		Map<String, String> cookieMap = new HashMap<String, String>();		
		Cookie[] c = request.getCookies();
		if (c != null) {
			for (Cookie cookie : c) {
				cookieMap.put(cookie.getName(), cookie.getValue());
			}
		}
		return cookieMap;
	}
	
	public static void setCookie(HttpServletResponse response, String name, String value) {
		Cookie cookie = new Cookie(name, value);
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		response.addCookie(cookie);
	}
}
