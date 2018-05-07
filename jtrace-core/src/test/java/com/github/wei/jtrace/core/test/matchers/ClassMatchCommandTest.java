package com.github.wei.jtrace.core.test.matchers;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.wei.jtrace.api.command.ICommandExecutor;
import com.github.wei.jtrace.api.command.ICommandResult;
import com.github.wei.jtrace.core.test.BaseTest;

public class ClassMatchCommandTest extends BaseTest{

	@Test
	public void testExtractMatchCommand() throws Exception{
		
		beanFactory.registBean(TestClassMatchCommand.class);
		ICommandExecutor commandExecutor = beanFactory.getBean(ICommandExecutor.class);
		
		ICommandResult result = commandExecutor.execute("test", "com.test.Test", "test(ILjava.lang.String;)","extract");
		
		beanFactory.destroyBean(TestClassMatchCommand.class);
		
		assertEquals(result.getResult(), "ok");
		
	}
	
	@Test
	public void testNameMatchCommand() throws Exception{
		
		beanFactory.registBean(TestClassMatchCommand.class);
		ICommandExecutor commandExecutor = beanFactory.getBean(ICommandExecutor.class);
		beanFactory.destroyBean(TestClassMatchCommand.class);
		
		ICommandResult result = commandExecutor.execute("test", "com.test.Test", "^.*test(ILjava.lang.String;)","extract");
		
		
		assertEquals(result.getResult(), "ok");
		
	}
	
	@Test
	public void testArgumentMatchCommand() throws Exception{
		
		beanFactory.registBean(TestClassMatchCommand.class);
		ICommandExecutor commandExecutor = beanFactory.getBean(ICommandExecutor.class);
		
		beanFactory.destroyBean(TestClassMatchCommand.class);
		
		ICommandResult result = commandExecutor.execute("test", "com.test.Test", "test(2)","extract");
		
		assertEquals(result.getResult(), "ok");
	}
}
