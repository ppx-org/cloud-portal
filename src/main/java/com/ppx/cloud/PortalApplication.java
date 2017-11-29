package com.ppx.cloud;

import javax.annotation.Resource;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;

import com.ppx.cloud.portal.filter.AccessFilter;

/**
 * 启动类
 * 
 * @author dengxz
 * @date 2017年11月20日
 */
@EnableCaching
@EnableEurekaServer
@EnableZuulProxy
@SpringCloudApplication
public class PortalApplication {

	// 为了让firstConfigBean先运行 (@ComponentScan自动扫描之后@Order不生效)
	@Resource(name = "firstConfigRun")
	private Object firstConfigBean;

	public static void main(String[] args) {
		new SpringApplicationBuilder(PortalApplication.class).web(true).run(args);
	}

	@Bean
	public AccessFilter accessFilter() {
		return new AccessFilter();
	}

}
