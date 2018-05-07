package com.github.wei.jtrace.core.test.config;

import java.net.URL;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.github.wei.jtrace.api.config.IConfigFactory;
import com.github.wei.jtrace.core.config.DefaultConfigFactory;
import com.github.wei.jtrace.core.util.AgentHelper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;


public class ConfigTest {

	@Test
	public void allConfig() throws Exception{
		URL url = AgentHelper.getAgentPropertiesFile();
		IConfigFactory configFactory = new DefaultConfigFactory(url);
		DefaultConfig config = configFactory.getConfig(DefaultConfig.class);
		
		Assert.assertEquals(config.getServerPort(), 3456);
		Assert.assertEquals(config.getTimeout(), 300);
	}
	
	@Test
	public void subConfig() throws Exception{
		IConfigFactory configFactory = new DefaultConfigFactory("jtrace.properties");
		SocketConfig config = configFactory.getConfig(SocketConfig.class);
		
		Assert.assertEquals(config.getTimeout(), 300);
		Assert.assertEquals(config.getConnections(), 2);
	}
	
	@Test
	public void testConfig() throws Exception{
		Config c = ConfigFactory.load("jtrace.properties");
		Config sub = c.getConfig("log.appender");
		Set<Entry<String, ConfigValue>> keys = sub.entrySet();
		
		for(Entry<String, ConfigValue> key: keys){
			System.out.println(key.getKey() + "= " + key.getValue().render());
		}
	}
}
