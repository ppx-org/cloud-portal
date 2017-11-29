package com.ppx.cloud.portal.grant.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mongodb.util.JSON;
import com.ppx.cloud.portal.cache.CacheConfiguration;
import com.ppx.cloud.portal.filter.FilterService;

@Service
public class ResourceService {
	
	@Resource(name="configMongoTemplate")
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private FilterService filterService;

	public Map<?, ?> getResource(Integer serviceId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(serviceId));
		
		Map<?, ?> map =  mongoTemplate.findOne(query, Map.class, "grant_resource");
		if (map == null) return null;
		return map;
	}
	
	public void saveResource(Integer serviceId, String tree, String removeIds) {
		// 清除过滤器中缓存
		filterService.clearCache(CacheConfiguration.URI_INDEX_CACHE);
		filterService.clearCache(CacheConfiguration.ROLE_BIT_SET_CACHE);
				
		if (!StringUtils.isEmpty(removeIds)) {
			String[] resId = removeIds.split(",");		
			Query removeQuery = new Query();
			removeQuery.addCriteria(Criteria.where("_id").in(new ArrayList<String>(Arrays.asList(resId))));
			mongoTemplate.remove(removeQuery, "grant_resource_uri");
		}	
		
		
		Update update = new Update();		
		update.set("tree", JSON.parse(tree));
		
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(serviceId));
		mongoTemplate.upsert(query, update, "grant_resource");
	}
	
	public Map<?, ?> getUri(String resId) {
		Query query = new Query();
		Criteria criteria = Criteria.where("_id").is(resId);
		query.addCriteria(criteria);
		return mongoTemplate.findOne(query, Map.class, "grant_resource_uri");
	}	
	
	private int saveToUri(int servcieId, String uri) {
		// 清除过滤器中缓存
		filterService.clearCache(CacheConfiguration.URI_INDEX_CACHE);
		filterService.clearCache(CacheConfiguration.ROLE_BIT_SET_CACHE);
		
		// 查看是否存在
		Query existsQuery = new Query();
		existsQuery.addCriteria(Criteria.where("serviceId").is(servcieId).and("uri").is(uri));
		Map<?, ?> existsMap = mongoTemplate.findOne(existsQuery, Map.class, "grant_uri");
		if (existsMap != null) {
			return (Integer)existsMap.get("_id");
		}
		else {
			int index = getUriSeq();
			
			Map<String, Object> saveMap = new HashMap<String, Object>();
			saveMap.put("_id", index);	
			saveMap.put("serviceId", servcieId);
			saveMap.put("uri", uri);
			mongoTemplate.save(saveMap, "grant_uri");
			return index;
		}
	}
	
	private int getUriSeq() {
		Query seqQuery = new Query();
		seqQuery.addCriteria(Criteria.where("_id").is("URI_SEQ"));
		Update update = new Update();
		update.inc("sequence_value", 1);
		Map<?, ?> seqMap = mongoTemplate.findAndModify(seqQuery, update, FindAndModifyOptions.options().upsert(true).returnNew(true)
				,Map.class, "grant_counters");
		int seq = (Integer)seqMap.get("sequence_value");
		return seq;
	}
	
	public Map<?, ?> saveUri(String resId, String uri, String menuId) {	
		// 清除过滤器中缓存
		filterService.clearCache(CacheConfiguration.URI_INDEX_CACHE);
		filterService.clearCache(CacheConfiguration.ROLE_BIT_SET_CACHE);
		
		int servcieId = Integer.parseInt(resId.split("_")[0]);		
		
		Update update = new Update();	
		String[] uriArray = uri.split(",");
		List<String> uriList = new ArrayList<String>();
		List<Integer> indexList = new ArrayList<Integer>();
		for (String u : uriArray) {
			uriList.add(u);
			int index = saveToUri(servcieId, u);
			indexList.add(index);
		}		
		update.addToSet("uri").each(uriList.toArray());
		update.addToSet("uriIndex").each(indexList.toArray());
		
		if (menuId != null) {
			update.set("pMenuId", menuId);
		}
		
		
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(resId));	
		Map<?, ?> map = mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().upsert(true).returnNew(true),
				Map.class, "grant_resource_uri");
		return map;
	}
	
	public void removeUri(String resId, String uri, int uriIndex) {
		// 清除过滤器中缓存
		filterService.clearCache(CacheConfiguration.URI_INDEX_CACHE);
		filterService.clearCache(CacheConfiguration.ROLE_BIT_SET_CACHE);
				
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(resId));
		if ("-1".equals(uri)) {
			mongoTemplate.remove(query, "grant_resource_uri");
		}
		else {
			Update update = new Update();	
			update.pull("uri", uri);
			update.pull("uriIndex", uriIndex);
			mongoTemplate.updateFirst(query, update, "grant_resource_uri");
		}
	}
}
