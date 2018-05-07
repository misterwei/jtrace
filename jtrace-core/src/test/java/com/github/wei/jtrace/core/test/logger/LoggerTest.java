package com.github.wei.jtrace.core.test.logger;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.core.logger.AllAppender;
import com.github.wei.jtrace.core.logger.ConsoleAppender;
import com.github.wei.jtrace.core.logger.FileAppender;
import com.github.wei.jtrace.core.logger.LoggerConfiger;
import com.github.wei.jtrace.core.test.BaseTest;

public class LoggerTest extends BaseTest{

	@Test
	public void testRootLogger() throws Exception{
		LoggerConfiger.removeConfiger(LoggerTest.class.getName());
		Logger logger = LoggerFactory.getLogger(LoggerTest.class);
		logger.info("this is a ConsoleLogger test");
		
		Thread.sleep(1000);
	}
	
	@Test
	public void testLoggerLevel() throws Exception{
		Logger logger = LoggerFactory.getLogger("levelTest");
		logger.info("this is a level info test");
		logger.debug("this is a level debug test");
		logger.error("this is a level error test");
		
		logger = LoggerFactory.getLogger("consoleTest");
		logger.info("this is a console level info test");
		logger.debug("this is a console level debug test");
		logger.error("this is a console level error test");
		
		Thread.sleep(1000);
	}
	
	@Test
	public void testFileLogger() throws Exception{
		LoggerConfiger.removeConfiger(LoggerTest.class.getName());
		
		FileAppender fa = new FileAppender("D:/jtrace.log");
		fa.open();
		
		LoggerConfiger.getConfiger("ROOT").setAppender(fa);
		
		Logger logger = LoggerFactory.getLogger(LoggerTest.class);
		logger.info("this is a FileLogger test");
		
		fa.close();
		
		Thread.sleep(1000);
	}
	
	@Test
	public void testFileLoggerRollover() throws Exception{
		LoggerConfiger.removeConfiger(LoggerTest.class.getName());
		
		FileAppender fa = new FileAppender("D:/jtrace.log");
		fa.setMaxSize(102400); //100K
		
		ConsoleAppender ca = new ConsoleAppender();
		
		AllAppender aa = new AllAppender();
		aa.addAppender(fa);
		aa.addAppender(ca);
		
		aa.open();
		
		LoggerConfiger.getConfiger("ROOT").setAppender(aa);
		
		Logger logger = LoggerFactory.getLogger(LoggerTest.class);
		for(int i=0;i<10000;i++){
			logger.info("this is a FileLogger rollover test {}", i);
		}
		aa.close();
		
		Thread.sleep(1000);
	}
	
	@Test
	public void testAllLogger() throws Exception{
		LoggerConfiger.removeConfiger(LoggerTest.class.getName());
		
		FileAppender fa = new FileAppender("D:/jtrace.log");
		ConsoleAppender ca = new ConsoleAppender();
		
		AllAppender aa = new AllAppender();
		aa.addAppender(fa);
		aa.addAppender(ca);
		
		aa.open();
		
		LoggerConfiger.getConfiger("ROOT").setAppender(aa);
		
		Logger logger = LoggerFactory.getLogger(LoggerTest.class);
		logger.info("this is a AllLogger test");
		
		aa.close();
		
		Thread.sleep(1000);
	}
	
	@Test
	public void multiThreadTest() throws Exception{
		LoggerConfiger.removeConfiger(LoggerTest.class.getName());
		
		FileAppender fa = new FileAppender("D:/jtrace.log");
		fa.setMaxSize(204800); //100K
		
		ConsoleAppender ca = new ConsoleAppender();
		
		AllAppender aa = new AllAppender();
		aa.addAppender(fa);
		aa.addAppender(ca);
		
		aa.open();
		
		LoggerConfiger.getConfiger("ROOT").setAppender(aa);
		
		final CountDownLatch countDown = new CountDownLatch(4);
		Runnable task = new Runnable() {
			
			@Override
			public void run() {
				Logger logger = LoggerFactory.getLogger(LoggerTest.class);
				for(int i=0;i<100;i++){
					logger.info("this is a multi thread rollover test {}", i);
				}
				countDown.countDown();
			}
		};
		
		Thread t1 = new Thread(task, "Thread1");
		Thread t2 = new Thread(task, "Thread2");
		Thread t3 = new Thread(task, "Thread3");
		Thread t4 = new Thread(task, "Thread4");
		
		t1.start();
		t2.start();
		t3.start();
		t4.start();
		
		countDown.await();
		
		aa.close();
		
		Thread.sleep(1000);
		
	}
	
	
	
}
