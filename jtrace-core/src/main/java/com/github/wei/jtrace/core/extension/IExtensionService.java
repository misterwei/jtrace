package com.github.wei.jtrace.core.extension;

public interface IExtensionService {
	
	void registerAttributeHandler(String attributeName, IAttributeHandler handler);
}
