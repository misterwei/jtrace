package com.github.wei.jtrace.weave;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.api.advice.AdviceMatcher;
import com.github.wei.jtrace.api.advice.AdviceMatcher.MatchType;
import com.github.wei.jtrace.api.advice.IAdviceManager;
import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.core.extension.AbstractExtensionService;
import com.github.wei.jtrace.core.extension.ExtensionJarInfo;

@Bean
public class WeaveService extends AbstractExtensionService {
	static Logger log = LoggerFactory.getLogger("WeaveService");
	
	@AutoRef
	private IAdviceManager adviceManager;
	
	public WeaveService() {
		super("weave");
	}

	@Override
	public String getId() {
		return "weave";
	}

	@Override
	protected void handle(ExtensionJarInfo jarInfo) {
		Map<String, Object> attrs = jarInfo.getAttributes();
		Object weaveSignature = attrs.get("Weave-Signature");
		

		List<AdviceMatcher.Builder> signatures = new ArrayList<AdviceMatcher.Builder>();
		if(weaveSignature != null) {
			List<?> result = (List<?>)weaveSignature;
			for(Object match : result) {
				Map<String,Object> map = (Map<String, Object>)match;
				String className = (String)map.get("class");
				Object method = map.get("method");
				
				AdviceMatcher.Builder builder = AdviceMatcher.newBuilder(className);
				builder.matchType(MatchType.EXTRACT);
				builder.noWeave();
				
				if(method != null) {
					if(method instanceof List) {
						List<String> methods = (List<String>)method;
						for(String m : methods) {
							builder.addMethod(m).end();
						}
					}else if(method instanceof String) {
						builder.addMethod(String.valueOf(method)).end();
					}
				}
				signatures.add(builder);
			}
		}
		
		List<AdviceMatcher.Builder> weaveListeners = new ArrayList<AdviceMatcher.Builder>();
		Object weaveAdvisor = attrs.get("Weave-Listener");
		if(weaveAdvisor != null) {
			List<Map<String, Object>> listeners = (List<Map<String, Object>>)weaveAdvisor;
			for(Map<String, Object> listener : listeners) {
				Map<String, Object> matcher = (Map<String, Object>)listener.get("matcher");
				String type = (String)matcher.get("type");
				String className = (String)matcher.get("class");
				
				AdviceMatcher.Builder builder = AdviceMatcher.newBuilder(className);
				if("base".equals(type)) {
					builder.matchType(MatchType.BASE);
				}else if("interface".equals(type)) {
					builder.matchType(MatchType.INTERFACE);
				}else {
					builder.matchType(MatchType.EXTRACT);
				}
				
				Object method = matcher.get("method");
				if(method instanceof String) {
					builder.addMethod(String.valueOf(method)).end();
				}else if(method instanceof List) {
					List<String> methods = (List<String>)method;
					for(String m : methods) {
						builder.addMethod(m).end();
					}
				}
				
				String listenerClass = (String)listener.get("listener");
				builder.setMessage(listenerClass);
				
				weaveListeners.add(builder);
			}
		}
		
		try {
			adviceManager.registAdviceListener(new WeaveAdviceListenerManager(this, signatures, weaveListeners, jarInfo));
		} catch (Exception e) {
			log.warn("regist advice listener failed", e);
		}
		
	}

}
