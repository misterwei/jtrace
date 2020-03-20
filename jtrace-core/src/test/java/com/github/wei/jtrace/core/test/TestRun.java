package com.github.wei.jtrace.core.test;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
		int i=0;
		try {
			if(i == 1){
				System.out.println("no1");
				return 1;
			}
			System.out.println("no");
			return 2;
		}finally {
			System.out.println("yes");
		}
	}


	public void testFor() {
		PrintStream ps = System.out;

		try {
			System.out.println(0);
		} finally {
			;
		}

		ps.println(1);
	}

	public void testFor2() {
		PrintStream ps = System.out;

		try {
			System.out.println(0);
		} finally {
			ps.println(1);
		}
	}

	private static void test(){
		try {
			return;
		}finally {
			System.out.println("aaa");
		}
	}
	
	private static void testArgs(String msg, Object...objects){
		System.out.println(msg + " " + objects.length);
	}

}
