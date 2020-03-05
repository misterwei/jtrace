package com.github.wei.jtrace.asm.api.matcher;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public class MethodAnnotationMatcher implements IMethodMatcher{
	private String annotation;
	public MethodAnnotationMatcher(String annotation) {
		this.annotation = annotation;
	}
	
	@Override
	public boolean match(MethodNode target) {
		List<AnnotationNode> anns = target.visibleAnnotations;
		if(anns == null) {
			return false;
		}
		
		for(AnnotationNode ann : anns) {
			String annName = Type.getType(ann.desc).getInternalName();
			if(annName.equals(annotation)) {
				return true;
			}
		}
		return false;
	}

}
