package com.github.wei.jtrace.core.test.yaml;

import java.util.Map;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

public class YamlTest {
	
	@Test
	public void testYaml() {
		Yaml yaml = new Yaml();
		Map<String, Object> result = yaml.load(YamlTest.class.getClassLoader().getResourceAsStream("test.yaml"));
		System.out.println(result);
	}
}
