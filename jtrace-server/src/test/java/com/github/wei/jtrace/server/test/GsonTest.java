package com.github.wei.jtrace.server.test;

import java.io.IOException;
import java.io.Serializable;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class GsonTest {

	@Test
	public void testGson(){
		@SuppressWarnings("rawtypes")
		Gson gson = new GsonBuilder().registerTypeAdapterFactory(TypeAdapters.newFactory(Class.class, new TypeAdapter<Class >() {

			@Override
			public void write(JsonWriter out, Class value) throws IOException {
				if (value == null) {
			         out.nullValue();
			         return;
			       }
				out.value(value.getName());
			}

			@Override
			public Class read(JsonReader in) throws IOException {
				// TODO Auto-generated method stub
				return null;
			}
		})).create();
		System.out.println(gson.toJson(new ClassTest(String.class)));
		Exception e = new IllegalAccessException("test");
		e.getStackTrace();
		e = new Exception(e);
		e.getStackTrace();
		System.out.println(gson.toJson(e));
	}
	
	public static class ClassTest implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Class<?> type;
		
		public ClassTest(Class<?> type) {
			this.type = type;
		}
		
		public Class<?> getType() {
			return type;
		}
	}
}
