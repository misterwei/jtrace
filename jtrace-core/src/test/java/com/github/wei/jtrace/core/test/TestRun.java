package com.github.wei.jtrace.core.test;

import java.io.File;

import com.google.common.io.Files;

public class TestRun {

	public static void main(String[] args) throws Exception{
		//JtraceLauncher launcher = new JtraceLauncher();
		//launcher.start(args, new TestInstrumentationBean());

//		Map<String,String> env = System.getenv();
//		Properties p = System.getProperties();
//		System.out.println(new Gson().toJson(env));
//		System.out.println(new Gson().toJson(p));
		
//		File f1 = new File("E:/shangxing.txt");
//		File f2 = new File("E:/shangxing.txt");
//		
//		System.out.println(f1.hashCode());
//		System.out.println(f2.hashCode());
//		System.out.println(f1.equals(f2));
		
//		testArgs("test");
//
//		Files.createParentDirs(new File("D:/test"));
	}

	private static int testIf(){
		Integer i = test(123);
		if(i == null){
			System.out.println(i);
			return 1;
		}
		System.out.println("no");
		return 2;
	}

	private static Integer test(int i){
		return 1;
	}
	
	private static void testArgs(String msg, Object...objects){
		System.out.println(msg + " " + objects.length);
	}

}
