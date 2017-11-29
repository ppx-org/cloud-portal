package com.ppx.cloud.portal.grant.grantto;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.ppx.cloud.common.jdbc.MyCriteria;
import com.ppx.cloud.common.jdbc.MyDaoSupport;
import com.ppx.cloud.common.jdbc.Page;
import com.ppx.cloud.portal.cache.CacheConfiguration;
import com.ppx.cloud.portal.filter.FilterService;

@Service
public class GrantToService extends MyDaoSupport {
	
	@Resource(name="configMongoTemplate")
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private FilterService filterService;
	
	public List<RoleBean> listRole(Page page, RoleBean role) {
		MyCriteria c = createCriteria("where")
			.addAnd("ROLE_ID = ?", role.getRoleId())
			.addAnd("ROLE_NAME like ?", "%", role.getRoleName(), "%");
		
		StringBuilder cSql = new StringBuilder("select count(*) from test_role").append(c);
		StringBuilder qSql = new StringBuilder("select * from test_role").append(c);
		
		return queryPage(RoleBean.class, page, cSql, qSql, c.getParaList());
	}
	
	public Map<?, ?> getAuthorize(Integer roleId, Integer serviceId) {		
		Query query = new Query();
		query.addCriteria(Criteria.where("roleId").is(roleId).and("serviceId").is(serviceId));
		return mongoTemplate.findOne(query, Map.class, "grant_authorize");
	}
	
	public void saveAuthorize(Integer roleId, Integer serviceId, String resIds) {
		// 清除过滤器中缓存
		filterService.clearCache(CacheConfiguration.ROLE_BIT_SET_CACHE);
				
		Update update = new Update();		
		update.set("resIds", resIds.split(","));
		
		Query query = new Query();
		query.addCriteria(org.springframework.data.mongodb.core.query.Criteria
				.where("roleId").is(roleId).and("serviceId").is(serviceId));
		mongoTemplate.upsert(query, update, "grant_authorize");
	}
	
	
	
}