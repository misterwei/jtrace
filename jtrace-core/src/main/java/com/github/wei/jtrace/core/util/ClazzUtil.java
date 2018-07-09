package com.github.wei.jtrace.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.github.wei.jtrace.api.clazz.ClassDescriber;
import com.github.wei.jtrace.core.clazz.ClassInfo;
import com.github.wei.jtrace.core.clazz.MethodDescriber;

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
		return new ClassDescriber(classNameToPath(clazz.getName()), modifier, classNameToPath(superClassName), interfaceClasses);
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
			methodInfos.add(new MethodDescriber(m.getName(), Type.getMethodDescriptor(m), m.getModifiers()));
		}
		
		info.setMethods(methodInfos);
		
		return info;
	}
	
	public static ClassDescriber extractClassDescriber(ClassReader classReader) {
		String modifier = ModifierUtil.toString(classReader.getAccess());
		return new ClassDescriber(classReader.getClassName(), modifier, classReader.getSuperName(), classReader.getInterfaces());
	}
	
	public static ClassInfo extractClassInfo(ClassReader classReader){
		
		ClassInfo classInfo = new ClassInfo();
		
		ClassDescriber descr = extractClassDescriber(classReader);
		
		classInfo.setClassDescriber(descr);
		
		classInfo.setMethods(extractMethodDescribers(classReader));
		
		return classInfo;
	}
	
	public static List<MethodDescriber> extractMethodDescribers(ClassReader classReader){
		final List<MethodDescriber> methods = new ArrayList<MethodDescriber>();

		classReader.accept(new ClassVisitor(Opcodes.ASM5) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature,
					String[] exceptions) {
				MethodDescriber methodInfo = new MethodDescriber(name, desc, access);
				methods.add(methodInfo);
				return super.visitMethod(access, name, desc, signature, exceptions);
			}
		}, ClassReader.EXPAND_FRAMES);	
		
		return methods;
	}
	
	/**
	 * 从ClassLoader中搜索Class字节流
	 * @param loader
	 * @param className
	 * @return 如果没有找到类则返回NULL
	 */
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
	 * @param classDescr
	 * @param methodDescr
	 * @return
	 */
	public static String getSignature(String className, String method, String desc) {
		StringBuilder sb = new StringBuilder();
		sb.append(className).append(method).append(desc);
		return sb.toString();
	}
	
}
