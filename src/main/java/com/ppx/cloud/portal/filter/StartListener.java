package com.ppx.cloud.portal.filter;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.netflix.discovery.DiscoveryClient;
import com.ppx.cloud.portal.log.AccessUtils;
import com.ppx.cloud.portal.log.LogService;

@Service
public class StartListener implements ApplicationListener<ContextRefreshedEvent> {

	@Autowired
	private WebApplicationContext app;

	public static WebApplicationContext staticContext;

	@Autowired
	public Environment env;

	@Autowired
	private LogService logService;
	

	class Setup extends Thread {
		@Override
		public void run() {
			try {
				Thread.sleep(5*1000);
				// 延期再注册
				DiscoveryClient.isOK = true;
			} catch (Exception e) {}
		}
	}
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// 防止启动报 java.net.ConnectException: 拒绝连接 (Connection refused)
		new Setup().start();
		
		StartListener.staticContext = app;

		////////////////////////
		// 最大连接数
		DataSource ds = (DataSource) app.getBean("dataSource");
		int maxActive = ds.getPoolProperties().getMaxActive();

		// 初始化对象
		OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory
				.getOperatingSystemMXBean();
		AccessUtils.setOperatingSystemMXBean(operatingSystemMXBean);
		Properties p = System.getProperties();

		/*
		 * 创建服务信息 微服务ID(由机器IP和端口组成) artifactId version osName 物理内存 硬盘大小
		 */
		Update machineUpdate = new Update();
		machineUpdate.set("artifactId", env.getProperty("info.app.artifactId"));
		machineUpdate.set("version", env.getProperty("info.app.version"));
		machineUpdate.set("osName", p.getProperty("os.name"));
		machineUpdate.set("totalPhysicalMemory", AccessUtils.getTotalPhysicalMemorySize());
		machineUpdate.set("freePhysicalMemory", AccessUtils.getFreePhysicalMemorySize());
		machineUpdate.set("totalSpace", AccessUtils.getTotalSpace());
		machineUpdate.set("springDatasourceUrl", env.getProperty("spring.datasource.url"));
		machineUpdate.set("maxActive", maxActive);
		machineUpdate.set("springDataMongodbUri", env.getProperty("spring.data.mongodb.uri"));
		machineUpdate.set("maxMemory", Runtime.getRuntime().maxMemory() / 1024 / 1024);
		machineUpdate.set("availableProcessors", Runtime.getRuntime().availableProcessors());
		Date now = new Date();
		machineUpdate.setOnInsert("firsted", now);
		machineUpdate.set("startLasted", now);
		machineUpdate.set("order", -1); // 排序
		machineUpdate.set("display", 1); // 显示/隐藏
		machineUpdate.set("type", "gateway");
		logService.upsertService(AccessUtils.getServiceId(), machineUpdate);

		// 启动日志 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		Map<String, Object> startMap = new LinkedHashMap<String, Object>();
		startMap.put("serviceId", AccessUtils.getServiceId());
		startMap.put("profiles", System.getProperty("spring.profiles.active"));
		startMap.put("startTime", new Date(ManagementFactory.getRuntimeMXBean().getStartTime()));
		startMap.put("artifactId", env.getProperty("info.app.artifactId"));
		startMap.put("version", env.getProperty("info.app.version"));
		startMap.put("springDatasourceUrl", env.getProperty("spring.datasource.url"));
		startMap.put("maxActive", maxActive);
		startMap.put("maxIdel", ds.getMaxIdle());
		startMap.put("springDataMongodbUri", env.getProperty("spring.data.mongodb.uri"));

		startMap.put("javaHome", p.getProperty("java.home"));
		startMap.put("javaRuntimeVersion", p.getProperty("java.runtime.version"));
		startMap.put("PID", p.getProperty("PID"));
		startMap.put("beanDefinitionCount", app.getBeanDefinitionCount());
		startMap.put("contextSpendTime", System.currentTimeMillis() - app.getStartupDate());
		startMap.put("jvmSpendTime", System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime());

		startMap.put("maxMemory", Runtime.getRuntime().maxMemory() / 1024 / 1024);
		startMap.put("totalMemory", Runtime.getRuntime().totalMemory() / 1024 / 1024);
		startMap.put("freeMemory", Runtime.getRuntime().freeMemory() / 1024 / 1024);

		// 服务个数
		RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping) app
				.getBean("requestMappingHandlerMapping");
		startMap.put("handlerMethodsSize", requestMappingHandlerMapping.getHandlerMethods().size());

		logService.insertStartLog(startMap);
		
		
	}
}
