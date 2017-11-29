package com.ppx.cloud.portal.cache;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.CacheManager;
import org.springframework.cache.guava.GuavaCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.cache.CacheBuilder;


/**
 * 缓存配置类
 * @author dengxz
 * @date 2017年11月22日
 */
@Configuration
public class CacheConfiguration {
	
	public static final String SERVICE_CACHE = "SERVICE_CACHE";	

	public static final String USER_ROLE_CACHE = "USER_ROLE_CACHE";
	
	public static final String URI_INDEX_CACHE = "URI_INDEX_CACHE";
	
	public static final String ROLE_BIT_SET_CACHE = "ROLE_BIT_SET_CACHE";
	
	public static final String MENU_OP_CACHE = "MENU_OP_CACHE";
	
	@Bean
    public CacheManager cacheManager() {		
        SimpleCacheManager manager = new SimpleCacheManager();         
        List<GuavaCache> list = new ArrayList<GuavaCache>();              
        list.add(buildServiceCache());
        list.add(buildUserRoleCache());
        list.add(buildUriIndexCache());
        list.add(buildRoleBitSetCache());
        list.add(buildMenOpCache());
        manager.setCaches(list);
        return manager;
    }
		
	
	private GuavaCache buildServiceCache() {
	      return new GuavaCache(CacheConfiguration.SERVICE_CACHE,
	      CacheBuilder.newBuilder().maximumSize(100).build());
	}
	
	private GuavaCache buildUserRoleCache() {
		return new GuavaCache(CacheConfiguration.USER_ROLE_CACHE,
			      CacheBuilder.newBuilder().maximumSize(1000).build());
	}
	
	
	private GuavaCache buildUriIndexCache() {
		return new GuavaCache(CacheConfiguration.URI_INDEX_CACHE,
			      CacheBuilder.newBuilder().maximumSize(1000).build());
	}
	
	private GuavaCache buildRoleBitSetCache() {
		return new GuavaCache(CacheConfiguration.ROLE_BIT_SET_CACHE,
			      CacheBuilder.newBuilder().maximumSize(1000).build());
	}
	
	private GuavaCache buildMenOpCache() {
		return new GuavaCache(CacheConfiguration.MENU_OP_CACHE,
			      CacheBuilder.newBuilder().maximumSize(100).build());
	}
	
}
