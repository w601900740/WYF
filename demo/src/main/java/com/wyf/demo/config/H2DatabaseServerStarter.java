/*
 * Domain Aqier.com Reserve Copyright
 * @author yuloang.wang@Aqier.com
 * @since 2017年12月16日
 */
package com.wyf.demo.config;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;

/**
 * H2 数据库配置 以及启动, 该类使用反射启动服务, 不需要强行依赖H2数据库jar包
 * @author yulong.wang@Aqier.com
 * @since 2017年12月16日
 */
@Configuration
public class H2DatabaseServerStarter {
	
	private static Log log = LogFactory.getLog(H2DatabaseServerStarter.class);
	
	private String port;

	private String baseDir;
	
	private boolean isStarted = false;

	private Class<?> serverClass;
	
	private Method createTcpServerMethod;
	
	private Method stopServerMethod;
	
	private Method startServerMethod;
	
	private Object server;
	
	public H2DatabaseServerStarter(Environment environment) {
		this.port = environment.getProperty("spring.datasource.port", "6379");
		this.baseDir = environment.getProperty("sprinig.cloud.datasource.url", "jdbc:h2:tcp://localhost:6379/~/aqier"); // jdbc:h2:tcp://localhost:6379/~/wyf
		int index = this.baseDir.lastIndexOf(":");
		this.baseDir = this.baseDir.substring(index);
		index = this.baseDir.indexOf("/");
		this.baseDir = this.baseDir.substring(index + 1);
		try {
			serverClass = Class.forName("org.h2.tools.Server");
			createTcpServerMethod = ReflectionUtils.findMethod(serverClass, "createTcpServer", new Class[]{String[].class});
			startServerMethod = ReflectionUtils.findMethod(serverClass, "start", new Class[0]);
			stopServerMethod = ReflectionUtils.findMethod(serverClass, "stop", new Class[0]);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		startServer();
	}

	private synchronized void startServer() {
		if (isStarted)
			return;
		try {
			log.info("正在启动h2 dataBase...");
			server = createTcpServerMethod.invoke(serverClass, new Object[] {new String[] {"-tcpPort", port, "-baseDir", baseDir}});
			startServerMethod.invoke(server, new Object[0]);
			isStarted = true;
			log.info("成功在端口" + port + ", dir: "+baseDir+", 上启动了 h2 dataBase...");
		} catch (Exception e) {
			Throwable ex = e.getCause() == null ? e : (Exception)e.getCause();
			log.warn("启动 h2 database Server 出错：" + ex.getMessage());
			if (ex.getMessage() == null || !ex.getMessage().contains("Address already in use")) {
				throw new RuntimeException(ex);
			}
		}
	}
	
	protected void stopServer() {
		if (server != null) {
			log.info("正在关闭h2 dataBase...");
			ReflectionUtils.invokeMethod(stopServerMethod, server);
			log.info("成功关闭了端口" + port + "上的 h2 dataBase...");
		}
		isStarted = false;
	}
}
