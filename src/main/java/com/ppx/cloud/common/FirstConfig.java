package com.ppx.cloud.common;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import com.mongodb.MongoClient;


/**
 * 第一次运行时配置，提高配置的可靠性和安全性
 * @author dengxz
 * @date 2017年11月14日
 */
@Configuration
public class FirstConfig {
	
	@Value("${spring.data.mongodb.host}")
	private String mongodbHost;
	
	@Value("${spring.data.mongodb.logName}")
	private String logName;
	
	@Value("${spring.data.mongodb.configName}")
	private String configName;
	
	/*
	 使用复制集时你需要知道的
	正确连接复制集的姿势
	MongoDB复制集里Primary节点是不固定的
	所以生产环境千万不要直连Primary
	mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]][/[database][?options]]
	mongodb:// 前缀，代表这是一个Connection String
	username:password@ 如果启用了鉴权，需要指定用户密码
	hostX:portX 复制集成员的ip:port信息，多个成员以逗号分割
	/database 鉴权时，用户帐号所属的数据库
	?options 指定额外的连接选项
	
	MongoClientURI connectionString = new MongoClientURI("mongodb://root:****@dds-bp114e3f1fc441342.mongodb.rds.aliyuncs.com:3717,dds-bp114e3f1fc441341.mongodb.rds.aliyuncs.com:3717/admin?replicaSet=mgset-677201"); // ****替换为root密码
	MongoClient client = new MongoClient(connectionString);
	MongoDatabase database = client.getDatabase("mydb");
	MongoCollection collection = database.getCollection("mycoll");
	
	 */
	
	@Bean
	@Primary
	public MongoTemplate logMongoTemplate() {
		// 连接log mongodb
		
		SimpleMongoDbFactory simpleMongoDbFactory = new SimpleMongoDbFactory(new MongoClient(mongodbHost), logName);
		// 去掉_class字段
		MappingMongoConverter converter = new MappingMongoConverter(new DefaultDbRefResolver(simpleMongoDbFactory),
				new MongoMappingContext());
		converter.setTypeMapper(new DefaultMongoTypeMapper(null));
		return new MongoTemplate(simpleMongoDbFactory, converter);
	}
	
	@Bean(name="configMongoTemplate")
	public MongoTemplate configMongoTemplate() {
		// 连接config mongodb
		SimpleMongoDbFactory simpleMongoDbFactory = new SimpleMongoDbFactory(new MongoClient(mongodbHost), configName);
		// 去掉_class字段
		MappingMongoConverter converter = new MappingMongoConverter(new DefaultDbRefResolver(simpleMongoDbFactory),
				new MongoMappingContext());
		converter.setTypeMapper(new DefaultMongoTypeMapper(null));
		return new MongoTemplate(simpleMongoDbFactory, converter);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean(name="firstConfigRun")
	public Object firstConfigRun() throws Exception {
		// 固定参数配置>>>>>>>>>>>>>>>>>>>>>
		// 非XML格式的html
		System.setProperty("spring.thymeleaf.mode", "LEGACYHTML5");

		MongoTemplate mongoTemplate = configMongoTemplate();
		// 加载banner，读取artifactId和version
		Properties bannerProp = new Properties();
		try (InputStream in = PropertiesConfig.class.getResourceAsStream("/banner.txt");) {
			bannerProp.load(in);
			System.setProperty("info.app.artifactId", bannerProp.getProperty("info.app.artifactId"));
			System.setProperty("info.app.version", bannerProp.getProperty("info.app.version"));
		}

		// 加载first.properties配置文件
		Properties properties = new Properties();
		try (InputStream in = PropertiesConfig.class.getResourceAsStream("/firstconfig/first.properties");) {
			properties.load(in);
		}
		Set<String> firstEnum = properties.stringPropertyNames();
		Map<String, String> firstMap = new LinkedHashMap<String, String>();
		for (String key : firstEnum) {
			firstMap.put(key, properties.getProperty(key));
		}

		// 不存在就插入，存在就跳过
		List<Map<String, Object>> insertList = new ArrayList<Map<String, Object>>();
		List<Map> listConf = mongoTemplate.findAll(Map.class, "config");
		
		Set<String> confKeySet = new HashSet<String>();
		for (Map<String, String> map : listConf) {
			confKeySet.add(map.get("_id"));
			System.setProperty(map.get("_id"),  map.get("value"));
		}
		for (String key : firstMap.keySet()) {
			if (!confKeySet.contains(key)) {
				Map<String, Object> insertMap = new LinkedHashMap<String, Object>();
				insertMap.put("_id", key);
				insertMap.put("value", firstMap.get(key));
				insertMap.put("created", new Date());
				insertList.add(insertMap);
				System.setProperty(key, firstMap.get(key));
			}
		}
		mongoTemplate.insert(insertList, "config");
		return null;
	}

}
