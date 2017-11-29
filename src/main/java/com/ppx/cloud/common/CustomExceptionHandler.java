package com.ppx.cloud.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

/**
 * 自定义异常处理
 * @author dengxz
 * @date 2017年11月21日
 */
@ControllerAdvice
public class CustomExceptionHandler implements HandlerExceptionResolver {

	@ExceptionHandler(value = Exception.class)
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object object,
			Exception exception) {
		
		response.setStatus(500);
		String accept = request.getHeader("accept");
		if (exception != null) exception.printStackTrace();
		if (accept != null && accept.indexOf("text/html") >= 0) {
			ControllerReturn.returnErrorHtml(response, 500, exception.getMessage());
		}
		else {
			ControllerReturn.returnErrorJson(response, 500, exception.getMessage());
		}
		
		
		return null;
	}

}
