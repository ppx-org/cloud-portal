package com.ppx.cloud.portal.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.ppx.cloud.common.ControllerReturn;
import com.ppx.cloud.common.PropertiesConfig;
import com.ppx.cloud.common.util.CookieUtils;
import com.ppx.cloud.portal.grant.PortalGrantUtils;
import com.ppx.cloud.portal.log.LogService;

/**
 * url过滤器,控制访问权限,日志留给微服务(减少网关的压力)，通过配置权限访问
 * @author dengxz
 * @date 2017年11月21日
 */
public class AccessFilter extends ZuulFilter {

	@Override
	public String filterType() {
		return "pre";
	}

	@Override
	public int filterOrder() {
		return 0;
	}

	@Override
	public boolean shouldFilter() {
		RequestContext ctx = RequestContext.getCurrentContext();
		HttpServletRequest request = ctx.getRequest();
		String uri = request.getRequestURI();
		
		ctx.getZuulRequestHeaders().put("Cookie", PortalGrantUtils.PPXTOKEN + "=" + CookieUtils.getCookieMap(request).get(PortalGrantUtils.PPXTOKEN));
		
		/**
		 * 不过滤带.的uri,带点的留给每个微服务拦截(在cloud-commons包的ControllerInterceptor中)
		 * getResourceUri为资源配置时取得每个服务的uri(在cloud-commons包的MonitorConfController中)
		 */
		if (uri.indexOf(".") > 0 || uri.endsWith("getResourceUri")) {
			return false;
		}
		return true;
	}

	/**
	 * 自定义过滤器的实现，需要继承ZuulFilter，需要重写实现下面四个方法：
	 * filterType：返回一个字符串代表过滤器的类型，在zuul中定义了四种不同生命周期的过滤器类型，具体如下： pre：可以在请求被路由之前调用
	 * routing：在路由请求时候被调用 post：在routing和error过滤器之后被调用 error：处理请求时发生错误时被调用
	 * filterOrder：通过int值来定义过滤器的执行顺序
	 * shouldFilter：返回一个boolean类型来判断该过滤器是否要执行，所以通过此函数可实现过滤器的开关。在上例中，
	 * 我们直接返回true，所以该过滤器总是生效。
	 * run：过滤器的具体逻辑。需要注意，这里我们通过ctx.setSendZuulResponse(false)令zuul过滤该请求，
	 * 不对其进行路由，然后通过ctx.setResponseStatusCode(401)设置了其返回的错误码，
	 * 当然我们也可以进一步优化我们的返回，比如，通过ctx.setResponseBody(body)对返回body内容进行编辑等。
	 */
	@SuppressWarnings({ "rawtypes" })
	@Override
	public Object run() {

		RequestContext ctx = RequestContext.getCurrentContext();
		HttpServletRequest request = ctx.getRequest();
		Integer userId = null;
		try {
			Map<String, String> cookieMap = CookieUtils.getCookieMap(request);
			String token = cookieMap.get(PortalGrantUtils.PPXTOKEN);
			// token为空,表示未登录
			if (StringUtils.isEmpty(token)) {
				return toLogin(ctx);
			}

			/**
			 * 较验tocken，并取得USER_ID, 在微服务中使用(不需要再校验) String userId =
			 * JWT.decode(token).getClaim("USER_ID").asString();
			 */
			Algorithm algorithm = Algorithm.HMAC256(PortalGrantUtils.getJwtPassword());
			JWTVerifier verifier = JWT.require(algorithm).build();
			DecodedJWT jwt = verifier.verify(token);
			userId = jwt.getClaim("USER_ID").asInt();

			FilterService filterService = (FilterService) StartListener.staticContext.getBean("filterService");

			String uri = request.getRequestURI();
			String[] uriItem = uri.split("/");
			if (uriItem.length < 4) {
				// /portal/mini/不包含功能路径则返回 HTTP 404 - Not Found
				response(ctx, 404, "Not Found.miss path", userId);
				return null;
			}
			String contextPath = "/" + uriItem[1] + "/" + uriItem[2];
			String functionUri = uri.replace(contextPath, "");

			/**
			 * 大中小权限 /* /uriItem/* /uriItem1/uriItem2 /uriItem1/uriItem2?q=1
			 */
			List<String> testUriList = new ArrayList<String>();
			testUriList.add("/*");
			testUriList.add("/" + uriItem[3] + "/*");
			testUriList.add(functionUri);
			String queryString = request.getQueryString();
			if (!StringUtils.isEmpty(queryString)) {
				// 只支持带一参数的
				String[] q = queryString.split("&");
				testUriList.add(functionUri + "?" + q[0]);
			}

			// 取得微服务ID
			int serviceId = filterService.getServiceId(contextPath);
			if (serviceId == -1) {
				response(ctx, 404, "Unauthorized.miss serviceId", userId);
				return null;
			}

			boolean missUri = true;
			// 取得URI对应的index
			for (String testUri : testUriList) {
				Integer index = filterService.getIndexFromUri(serviceId, testUri);
				if (index == null) {
					continue;
				}
				// 判断角色是否有uri的权限
				List<Integer> listRoleId = filterService.getRoles(userId);
				for (Integer roleId : listRoleId) {
					BitSet grantBitset = filterService.getRoleResBitSet(serviceId, roleId);
					// 合法访问
					if (grantBitset.get(index)) {
						// uri合法访问, 写入到header 通过request.setAttribute设置操作权限 真实URI
						List<Map> actionList = filterService.getOpUri(serviceId, functionUri);
						setPermitAction(ctx, grantBitset, actionList);
						return null;
					}
				}
				missUri = false;
			}

			if (missUri) {
				// 从mongodb找不到对应uri,资源对应的uri找不到
				response(ctx, 404, "Unauthorized.miss uri", userId);
			} else {
				// HTTP 403 - 禁止访问
				response(ctx, 403, "Unauthorized.forbidden", userId);
			}
		} catch (SignatureVerificationException e) {
			response(ctx, 403, "Unauthorized.token error", userId);
			e.printStackTrace();
		} catch (Exception e) {
			response(ctx, 403, "Unauthorized.exception." + e.getMessage(), userId);
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void setPermitAction(RequestContext ctx, BitSet grantBitset, List<Map> actionList) {
		for (Map<?, ?> map : actionList) {
			for (Integer uriIndex : (List<Integer>)map.get("uriIndex")) {
				if (grantBitset.get(uriIndex)) {
					// /test/saveTest改名成_test_saveTest
					List<String>uriList = (List<String>)map.get("uri");
					String permitAction = StringUtils.arrayToCommaDelimitedString(uriList.toArray());
					if (!StringUtils.isEmpty(permitAction)) {
						ctx.getZuulRequestHeaders().put(PortalGrantUtils.PERMITACTION, permitAction);
					}
				}
			}
		}
	}

	private Object toLogin(RequestContext ctx) {
		try {
			ctx.setSendZuulResponse(false);
			ctx.setResponseStatusCode(401);
			String accept = ctx.getRequest().getHeader("accept");
			if (accept != null && accept.indexOf("text/html") >= 0) {
				// 跳转到登录页面
				ctx.getResponse().sendRedirect(PropertiesConfig.contextPath + "/login");
			} else {
				// json返回 HTTP 401 - 未授权：登录失败
				ControllerReturn.returnErrorJson(ctx.getResponse(), 401, "Not Login");
			}

			// 写入日志到mongodb
			LogService logService = (LogService) StartListener.staticContext.getBean("logService");
			logService.saveExceptionAccessLog(ctx.getRequest(), 401, "Not Login", null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void response(RequestContext ctx, int code, String msg, Integer userId) {
		// HTTP 401 - 未登录
		// HTTP 403 - 禁止访问
		ctx.setSendZuulResponse(false);
		ctx.setResponseStatusCode(code);
		String accept = ctx.getRequest().getHeader("accept");
		if (accept != null && accept.indexOf("text/html") >= 0) {
			ctx.setResponseBody("[" + code + "]System Information[" + msg + "]");
			ctx.setResponseStatusCode(code);
		} else {
			// json返回
			ControllerReturn.returnErrorJson(ctx.getResponse(), code, msg);
		}

		// 写入日志到mongodb
		LogService logService = (LogService) StartListener.staticContext.getBean("logService");
		logService.saveExceptionAccessLog(ctx.getRequest(), code, msg, userId);
	}
}
