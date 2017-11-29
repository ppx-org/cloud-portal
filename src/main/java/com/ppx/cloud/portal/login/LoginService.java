package com.ppx.cloud.portal.login;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.ppx.cloud.common.jdbc.MyDaoSupport;
import com.ppx.cloud.portal.grant.PortalGrantUtils;

@Service
public class LoginService extends MyDaoSupport {
	
	public Map<String, Object> getUser(String a, String p) {
		String countSql = "select count(*) from test_user where USER_ACCOUNT = ? and USER_PASSWORD = md5(?)";		
		int c = getJdbcTemplate().queryForObject(countSql, Integer.class, a, PortalGrantUtils.getMixUserPassword(p));
		if (c == 0) return null;
		
		String sql = "select * from test_user where USER_ACCOUNT = ? and USER_PASSWORD = md5(?)";
		Map<String, Object> userMap = getJdbcTemplate().queryForMap(sql, a, PortalGrantUtils.getMixUserPassword(p));
		return userMap;
	}
}