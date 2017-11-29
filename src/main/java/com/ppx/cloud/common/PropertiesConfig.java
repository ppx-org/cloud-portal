package com.ppx.cloud.common;

import javax.annotation.Resource;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * tips:setRemoveAbandoned超时是否删除连接，在处理量大的数据时不能用true
 * @author dengxz
 * @date 2017年11月21日
 */
@Configuration
public class PropertiesConfig {
	
	// 为了让firstConfigBean先运行 (@ComponentScan自动扫描之后@Order不生效)
	@Resource(name = "firstConfigRun")
	private Object firstConfigBean;		

	@Autowired
	public Environment env;
	
	public static String contextPath;
	
	public static String serverPort;
	
	@Bean
	public DataSource dataSource() {
		
		PropertiesConfig.contextPath = env.getProperty("server.context-path");
		PropertiesConfig.serverPort = env.getProperty("server.port");
		System.setProperty("jwt.password", env.getProperty("jwt.password"));		
		
		PoolProperties p = new PoolProperties();
		p.setUrl(env.getProperty("spring.datasource.url"));
		p.setDriverClassName("com.mysql.jdbc.Driver");
		p.setUsername(env.getProperty("spring.datasource.username"));
		p.setPassword(env.getProperty("spring.datasource.password"));
		p.setMaxActive(3);
		// setRemoveAbandoned超时是否删除连接(秒)
		p.setRemoveAbandonedTimeout(600);
		p.setLogAbandoned(true);
		// setRemoveAbandoned超时是否删除连接，在处理量大的数据时不能用true
		p.setRemoveAbandoned(true);
		p.setInitialSize(0);

		p.setJmxEnabled(false);
		p.setTestWhileIdle(false);
		p.setTestOnBorrow(true);
		p.setValidationQuery("SELECT 1");
		p.setTestOnReturn(false);
		p.setValidationInterval(30000);
		p.setTimeBetweenEvictionRunsMillis(30000);
		p.setMaxWait(10000);
		p.setMinEvictableIdleTimeMillis(30000);
		p.setMinIdle(10);
		
		DataSource datasource = new DataSource();
		datasource.setPoolProperties(p);
		return datasource;
	}

}
