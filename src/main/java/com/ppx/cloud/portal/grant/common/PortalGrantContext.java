package com.ppx.cloud.portal.grant.common;

/**
 * 分配权限上下文
 * @author dengxz
 * @date 2017年11月10日
 */
public class PortalGrantContext {

	public static ThreadLocal<LoginUser> threadLocalLongUser = new ThreadLocal<LoginUser>();

	public static void setLoginUser(LoginUser u) {
		threadLocalLongUser.set(u);
	}
	
	public static LoginUser getLoginUser() {
		return threadLocalLongUser.get();
	}
	

}
