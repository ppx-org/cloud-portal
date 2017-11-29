package com.ppx.cloud.common;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 统一返回JSON、成功失败等页面
 * @author dengxz
 * @date 2017年11月21日
 */
public class ControllerReturn {
	
	/**
	 * actionStatus:OK表示处理成功，FAIL表示失败，如果为FAIL，ErrorInfo带上失败原因
	 * errorCode:0为成功，其他为失败
	 * errorInfo:失败原因
	 */
	
	/**
	 * 成功时返回的json 
	 * @param obj 默认使用对象名称，需要改名可以使用Map<String, Object>传入
	 * @return
	 */
	public static Map<String, Object> ok(Object... obj) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("actionStatus", "OK");
		map.put("errorCode", "0");
		for (Object o : obj) {
			if (o instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, Object> m = (Map<String, Object>) o;
				Set<String> set = m.keySet();
				for (String key : set) {
					map.put(key, m.get(key));
				}
			} else {
				String keyName = o.getClass().getSimpleName();
				keyName = "Integer".equals(keyName) ? "result" : keyName;
				keyName = keyName.substring(0,1).toLowerCase() + keyName.substring(1);
				map.put(keyName, o);
			}
		}
		return map;
	}
	
	/**
	 * 失败时返回的json 
	 * @param obj
	 * @return
	 */
	public static Map<String, Object> fail(int errorCode, String errorInfo) {
		Map<String, Object> m = new HashMap<String, Object>(3);
		m.put("actionStatus", "FAIL");
		m.put("errorCode", errorCode);
		m.put("errorInfo", errorInfo);
		return m;
	}
	
	
	/**
	 * 返回错误(HMTL格式)
	 * @param response
	 * @param errorCode
	 * @param errorInfo
	 */
	public static void returnErrorHtml(HttpServletResponse response, Integer errorCode, String errorInfo) {
		// html返回
		PrintWriter pw = null;
		try {
			pw = response.getWriter();
			pw.write("[" + errorCode + "]" + "System Information[" + errorInfo + "]");
			pw.close();
		} catch (Exception e) {	
			e.printStackTrace();
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}
	
	/**
	 * 返回错误(JSON格式)
	 * @param response
	 * @param errorCode
	 * @param errorInfo
	 */
	public static void returnErrorJson(HttpServletResponse response, Integer errorCode, String errorInfo) {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");
		Map<String, Object> map = ControllerReturn.fail(errorCode, errorInfo);
		PrintWriter pw = null;
		try {
			String returnJson = new ObjectMapper().writeValueAsString(map);
			pw = response.getWriter();
			pw.write(returnJson);
		} catch (Exception e) {			
			e.printStackTrace();
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}
}
