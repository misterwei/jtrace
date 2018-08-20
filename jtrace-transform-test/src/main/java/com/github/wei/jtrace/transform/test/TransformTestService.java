package com.github.wei.jtrace.transform.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.config.IConfig;
import com.github.wei.jtrace.api.service.IService;
import com.github.wei.jtrace.core.transform.TransformService;

@Bean
public class TransformTestService implements IService{
	Logger log = LoggerFactory.getLogger("TransformTestService");
	
	@AutoRef
	private TransformService transformService;
	
	public String getId() {
		return "transform-test";
	}

	public boolean start(IConfig config) {
		try {
			transformService.registTransformer(new TransformTest(), false);
			transformService.registTransformer(new TransformTest2(), true);
		} catch (IllegalAccessException e) {
			log.error("transform-test regist failed ", e);
		}
		return true;
	}

}
