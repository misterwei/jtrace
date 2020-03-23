package com.github.wei.jtrace.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.github.wei.jtrace.api.clazz.ClassDescriber;
import com.github.wei.jtrace.api.clazz.MethodDescriber;
import com.github.wei.jtrace.core.clazz.ClassInfo;

public class ClazzUtil {

	public static ClassDescriber extractClassDescriber(Class<?> clazz) {
		
		Class<?> supperClazz = clazz.getSuperclass();
		String modifier = Modifier.toString(clazz.getModifiers());
		
		String[] interfaceClasses = null;
		Class<?>[] interfaces = clazz.getInterfaces();
		if(interfaces != null) {
			interfaceClasses = new String[interfaces.length];
			for(int i=0;i<interfaces.length; i++){
				Class<?> inter = interfaces[i];
				interfaceClasses[i] = classNameToPath(inter.getName());
			}
		}
		String superClassName = supperClazz == null? null : supperClazz.getName();
		
		String[] anns = null;
		Annotation[]  annotations = clazz.getAnnotations();
		if(annotations != null) {
			anns = new String[annotations.length];
			for(int i=0;i<annotations.length;i++) {
				Annotation a = annotations[i];
				anns[i] = classNameToPath(a.annotationType().getName());
			}
		}
		
		return new ClassDescriber(classNameToPath(clazz.getName()), modifier, classNameToPath(superClassName), interfaceClasses, anns);
	}
	
	public static ClassInfo extractClassInfo(Class<?> clazz){
		ClassInfo info = new ClassInfo();
		
		ClassLoader loader = clazz.getClassLoader();
		if(loader != null){
			info.setClassLoader(loader.toString());
		}
		
		ClassDescriber descr = extractClassDescriber(clazz);
		info.setClassDescriber(descr);
		
		Method[] methods = clazz.getDeclaredMethods();
		
		List<MethodDescriber> methodInfos = new ArrayList<MethodDescriber>(methods.length);
		for(int i=0;i<methods.length;i++){
			Method m = methods[i];
			
			String desc = Type.getMethodDescriptor(m).replace('.', '/');
			
			String[] anns = null;
			Annotation[]  annotations = clazz.getAnnotations();
			if(annotations != null) {
				anns = new String[annotations.length];
				for(int j=0;j<annotations.length;j++) {
					Annotation a = annotations[j];
					anns[j] = classNameToPath(a.annotationType().getName());
				}
			}
			
			methodInfos.add(extractMethodDescriber(m.getModifiers(), m.getName(), desc, anns));
		}
		
		info.setMethods(methodInfos);
		
		return info;
	}
	
	public static ClassDescriber extractClassDescriber(ClassReader classReader) {
		String modifier = ModifierUtil.toString(classReader.getAccess());
		final List<String> annList = new ArrayList<String>(10);
		classReader.accept(new ClassVisitor(Opcodes.ASM5) {
			@Override
			public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
				String className = Type.getType(desc).getInternalName();
				if(!annList.contains(className)) {
					annList.add(className);
				}
				return super.visitAnnotation(desc, visible);
			}
			
		}, ClassReader.EXPAND_FRAMES);
		
		String[] anns = null;
		if(annList.size() > 0) {
			anns = annList.toArray(new String[annList.size()]);
		}
		return new ClassDescriber(classReader.getClassName(), modifier, classReader.getSuperName(), classReader.getInterfaces(), anns);
	}
	
	public static ClassInfo extractClassInfo(ClassReader classReader){
		
		ClassInfo classInfo = new ClassInfo();
		
		ClassDescriber descr = extractClassDescriber(classReader);
		
		classInfo.setClassDescriber(descr);
		
		classInfo.setMethods(extractMethodDescribers(classReader));
		
		return classInfo;
	}
	
	public static MethodDescriber extractMethodDescriber(int access, String name, String desc, String[] annotations) {
		Type[] types = Type.getArgumentTypes(desc);
		String[] argumentTypes  = null;
		if(types != null) {
			argumentTypes = new String[types.length]; 
			for(int j=0;j< types.length;j++) {
				argumentTypes[j] = types[j].getClassName().replace('.', '/');
			}
		}
		
		String returnType = Type.getReturnType(desc).getClassName().replace('.', '/');
		String modifier = ModifierUtil.toString(access);
		
		return new MethodDescriber(name, modifier, argumentTypes, returnType, desc, annotations);
	}
	
	public static List<MethodDescriber> extractMethodDescribers(ClassReader classReader){
		final List<MethodDescriber> methods = new ArrayList<MethodDescriber>();
		
		ClassNode cn = new ClassNode(Opcodes.ASM5);
		classReader.accept(cn, ClassReader.EXPAND_FRAMES);
		List<MethodNode> mns = cn.methods;
		
		for(MethodNode mn : mns) {
			List<String> annList = new ArrayList<String>(10);
			List<AnnotationNode> ans = mn.visibleAnnotations;
			if(ans != null) {
				for(AnnotationNode an : ans) {
					annList.add(Type.getType(an.desc).getInternalName());
				}
			}
			
			String[] anns = null;
			if(annList.size() > 0) {
				anns = annList.toArray(new String[annList.size()]);
			}
			
			MethodDescriber methodInfo = extractMethodDescriber(mn.access, mn.name, mn.desc, anns);
			methods.add(methodInfo);
		}
		
		return methods;
	}

	public static ClassDescriber extractClassDescriber(URL url) throws IOException{
		if(url == null) {
			return null;
		}
		InputStream in = url.openStream();
		try {
			ClassReader cr = new ClassReader(in);
			return ClazzUtil.extractClassDescriber(cr);
		}finally {
			try {
				in.close();
			} catch (Exception e) {}
		}
	}
	
	/**
	 * 将class name 转为 class path
	 * . to /
	 * @param name
	 * @return
	 */
	public static String classNameToPath(String name) {
		if(name != null) {
			return name.replace('.', '/');
		}
		return null;
	}
	
	public static String[] classNameToPath(String[] names) {
		if(names != null) {
			for(int i=0;i<names.length;i++) {
				names[i] = names[i].replace('.', '/');
			}
			return names;
		}
		return null;
	}
	
	/**
	 * 
	 * @param className
	 * @return 如果没有目录则返回null
	 */
	public static String[] splitClassPathAndFile(String className) {
		int lastIndexOf = className.lastIndexOf("/");
		if(lastIndexOf < 0) {
			return null;
		}
		return new String[]{className.substring(0, lastIndexOf),className.substring(lastIndexOf+1)};
	}
	
	public static boolean classNameEquals(String name1, String name2) {
		return name1 != null ? classNameToPath(name1).equals(classNameToPath(name2)) : name2 == null;
	}

	public static boolean isExcludes(ClassLoader loader, String className){
		className = classNameToPath(className);
		if(className.startsWith("com/sun") || className.startsWith("$")){
			return true;
		}
		return isJtraceClass(loader, className);
	}

	public static boolean isJtraceClass(ClassLoader loader, String className) {
		if(loader != null && loader.getClass().getName().equals(Constants.AGENTCLASSLOADER_CLASSNAME)){
			return true;
		}
		if(className.startsWith(Constants.AGENTCLASS_PATHPREFIX)) {
			return true;
		}
		if(className.startsWith(Constants.AGENTCLASS_PREFIX)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 获取签名
	 * @return
	 */
	public static String getSignature(String className, String method, String desc) {
		StringBuilder sb = new StringBuilder();
		sb.append(className).append(method).append(desc);
		return sb.toString();
	}
	
}
