package com.ppx.cloud.portal.grant;

public class PortalGrantUtils {
	
	public static String PPXTOKEN = "PPXTOKEN";
	
	public static String PERMITACTION = "PERMITACTION";
	
	public static String getJwtPassword() {
		return System.getProperty("jwt.password") + "PASS";
	}
	
	public static String getMixUserPassword(String p) {
		return p + "PASS";
	}
}
