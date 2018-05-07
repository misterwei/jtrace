package com.github.wei.jtrace.core.test.reflect;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Reflect {

	public Reflect(List<Integer> a){
		
	}
	
	public void newInstance(){

		
		Constructor[] cons = Test.class.getConstructors();
		
		for(Constructor con : cons){
			System.out.println(Arrays.toString(con.getParameterTypes()));
			System.out.println(Arrays.toString(con.getGenericParameterTypes()));
		}
	}
	
	class Test{
		public Test(List<Integer> a){
			
		}
	}
	
	public static void main(String[] args) {
		Reflect r = new Reflect(new ArrayList());
		Constructor[] cons = r.getClass().getConstructors();
		
		for(Constructor con : cons){
			System.out.println(Arrays.toString(con.getParameterTypes()));
			System.out.println(Arrays.toString(con.getGenericParameterTypes()));
		}
		
		r.newInstance();
	}

}
