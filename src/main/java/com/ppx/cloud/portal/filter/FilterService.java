package com.ppx.cloud.portal.filter;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.ppx.cloud.common.jdbc.MyDaoSupport;
import com.ppx.cloud.portal.cache.CacheConfiguration;


@Service
public class FilterService extends MyDaoSupport {
	
	@Resource(name="configMongoTemplate")
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private CacheManager cacheManager;
	
	public void clearCache(String cacheName) {
		Cache cache = cacheManager.getCache(cacheName);
		cache.clear();
	}
	
	@Cacheable(value=CacheConfiguration.SERVICE_CACHE, key="#contextPath")
	public Integer getServiceId(String contextPath) {
		Query query = new Query();
		query.addCriteria(Criteria.where("contextPath").is(contextPath));
		Map<?, ?> map = mongoTemplate.findOne(query, Map.class, "grant_service");
		if (map == null || map.get("_id") == null) return -1;
		else return (Integer)map.get("_id");
	}

	@Cacheable(value=CacheConfiguration.USER_ROLE_CACHE)
	public List<Integer> getRoles(Integer userId) {
		String sql = "select ROLE_ID from test_user_in_role where USER_ID = ?";		
		List<Map<String, Object>> listRole = getJdbcTemplate().queryForList(sql, userId);
		List<Integer> returnList = new ArrayList<Integer>();
		for (Map<String, Object> map : listRole) {
			Integer roleId = (Integer)map.get("ROLE_ID");
			returnList.add(roleId);
		}
		return returnList;		
	}	
	
	@Cacheable(value=CacheConfiguration.URI_INDEX_CACHE)
	public Integer getIndexFromUri(Integer serviceId, String uri) {
		Query query = new Query();
		Criteria criteria = Criteria.where("serviceId").is(serviceId).and("uri").in(uri);
		query.addCriteria(criteria);
		
		Map<?, ?> map = mongoTemplate.findOne(query, Map.class, "grant_uri");
		if (map != null) {
			Integer index = (Integer)map.get("_id");
			return index;	
		}
		return null;
	}
	
	@Cacheable(value=CacheConfiguration.ROLE_BIT_SET_CACHE)
	public BitSet getRoleResBitSet(Integer serviceId, Integer roleId) {		
		BitSet grantBitset = new BitSet();
	    
    	List<String> resIds = getResIds(serviceId, roleId);
    	List<Integer> uriIndexes  = getUriIndexes(resIds);
    	for (Integer index : uriIndexes) {
    		grantBitset.set(index);
		}
    	return grantBitset;
	}
	
	@SuppressWarnings("rawtypes")
	@Cacheable(value=CacheConfiguration.MENU_OP_CACHE)
	public List<Map> getOpUri(int serviceId, String menuUri) {
		// 在grant_resource_uri中，如果是操作项将有pMenuId字段
		Query query = new Query();
		query.addCriteria(Criteria.where("uri").in(menuUri));
		
		List<Map> list = mongoTemplate.find(query, Map.class, "grant_resource_uri");		
		List<String> menuList = new ArrayList<String>();
		for (Map map : list) {
			// 判断是serviceId开头的
			String menuId = (String)map.get("_id");
			if (menuId.startsWith(serviceId + "")) {
				menuList.add(menuId);
			}
		}
	
		Query opQuery = new Query();
		opQuery.addCriteria(Criteria.where("pMenuId").in(menuList));
		List<Map> opList = mongoTemplate.find(opQuery, Map.class, "grant_resource_uri");
		return opList;
	}
	
	
	@SuppressWarnings("unchecked")
	private List<String> getResIds(Integer serviceId, Integer roleId) {
		Query query = new Query();
		Criteria criteria = Criteria.where("roleId").is(roleId);
		query.addCriteria(criteria);
		
		Map<?, ?> map = mongoTemplate.findOne(query, Map.class, "grant_authorize");
		if (map != null) {
			return (List<String>)map.get("resIds");
		}
		else {
			return new ArrayList<String>();
		}
	}
	
	@SuppressWarnings("rawtypes")
	private List<Integer> getUriIndexes(List<String> resIds) {
		Query query = new Query();
		Criteria criteria = Criteria.where("_id").in(resIds);
		query.addCriteria(criteria);
		
		List<Integer> returnList = new ArrayList<Integer>();
		List<Map> list = mongoTemplate.find(query, Map.class, "grant_resource_uri");
		if (list != null) {
			for (Map map : list) {
				@SuppressWarnings("unchecked")
				List<Integer> uriList = (List<Integer>)map.get("uriIndex");
				returnList.addAll(uriList);
			}
		}
		return returnList;
	}

	


}