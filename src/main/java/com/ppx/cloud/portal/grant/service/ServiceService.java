package com.ppx.cloud.portal.grant.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.ppx.cloud.portal.cache.CacheConfiguration;
import com.ppx.cloud.portal.filter.FilterService;

@Service
public class ServiceService {
	
	@Resource(name="configMongoTemplate")
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private FilterService filterService;
	
	@SuppressWarnings("rawtypes")
	public List<Map> listService() {
		String collectionName = "grant_service";
		Query query = new Query();
		query.with(new Sort(new Order(Direction.ASC, "order")));
		
		return mongoTemplate.find(query, Map.class, collectionName);
	}
	
	public void addService(String serviceName, String contextPath) {
		// 清除过滤器中缓存
		filterService.clearCache(CacheConfiguration.SERVICE_CACHE);
		
		String collectionName = "grant_service";
		long count = mongoTemplate.count(new Query(), collectionName);
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("_id", new Integer(count + ""));
		map.put("serviceName", serviceName);
		map.put("contextPath", contextPath);		
		map.put("order", count);
		map.put("lasted", new Date());
		
		mongoTemplate.insert(map, collectionName);
	}
	
	public void updateService(Integer _id, String serviceName, String contextPath) {
		// 清除过滤器中缓存
		filterService.clearCache(CacheConfiguration.SERVICE_CACHE);
		
		Update update = new Update();
		update.set("serviceName", serviceName);
		update.set("contextPath", contextPath);
		update.set("lasted", new Date());
		
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(_id));
		
		mongoTemplate.updateFirst(query, update, "grant_service");
	}
	
	public int removeService(Integer _id) {
		// 清除过滤器中缓存
		filterService.clearCache(CacheConfiguration.SERVICE_CACHE);
				
		// 查看关联资源
		Query resQuery = new Query();
		resQuery.addCriteria(Criteria.where("_id").is(_id));
		long count = mongoTemplate.count(resQuery, "grant_resource");
		if (count == 1) {
			return -1;
		}		
		
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(_id));
		mongoTemplate.remove(query, "grant_service");
		return 1;
	}
	
	public void orderService(String serviceIds) {
		String[] serviceId = serviceIds.split(",");
		for (int i = 0; i < serviceId.length; i++) {
			Update update = new Update();
			update.set("order", i);
			
			Query query = new Query();
			query.addCriteria(Criteria.where("_id").is(new Integer(serviceId[i])));
			mongoTemplate.updateFirst(query, update, "grant_service");
		}
	}
	
}
