package com.ppx.cloud.portal.log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class LogService {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	public void saveExceptionAccessLog(HttpServletRequest request, Integer code, String msg, Integer userId) {
		saveExceptionAccessLog(request, null, code, msg, userId);
	}

	public void saveExceptionAccessLog(HttpServletRequest request, String uri, Integer code, String msg, Integer userId) {
		
		// 日志记录
		AccessEntity access = new AccessEntity();
		access.setBeginTime(new Date());
		access.setSpendTime(0);
		access.setIp(AccessUtils.getIpAddress(request));
		if (uri == null) {
			access.setUri(request.getRequestURI());
		}
		else {
			access.setUri(uri);
		}
		
		access.setQueryString(request.getQueryString());
		access.setUserId(userId + "");

		long useMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024;
		access.setUseMemory(useMemory);

		access.setServiceId(AccessUtils.getServiceId());

		if (code != null) {
			ErrorBean error = new ErrorBean(code, msg);
			access.setError(error);
		}
		
		mongoTemplate.insert(access, "access_" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
	}
	
	// 机器信息
	public void upsertService(String serviceId, Update update) {
		Query query = new Query();
		Criteria criteria = Criteria.where("_id").is(serviceId);
		query.addCriteria(criteria);
		
		mongoTemplate.upsert(query, update, "service");
	}
	
	// 启动日志
	public void insertStartLog(Map<String, Object> map) {		
		mongoTemplate.insert(map, "start");
	}
	
	

}