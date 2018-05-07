package com.github.wei.jtrace.core.logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import com.google.common.io.Files;

public class FileAppender implements IAppender {
	private File file;
	private String filePath;
	
	private String lineSeparator = System.getProperty("line.separator");
	
	private int maxSize = 100000000; //100M
	private String charset = "utf-8";
	
	private int rollIndex = 0;
	
	private FileOutputStream outStream;
	
	public FileAppender(String filePath){
		this.filePath = filePath;
		this.file = new File(filePath);
		
		if(lineSeparator == null || lineSeparator.isEmpty()){
			lineSeparator = "\r\n";
		}
	}
	
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	@Override
	public void open() throws Exception{
		if(this.outStream == null){
			//创建父目录
			Files.createParentDirs(file);
			this.outStream = new FileOutputStream(file, true);
		}
	}

	@Override
	public synchronized void append(String msg) {
		FileChannel fc = outStream.getChannel();
		if (fc == null) {
			return;
		}

		FileLock fileLock = null;
		try {
			fileLock = fc.lock();
			long position = fc.position();
			long size = fc.size();
			if (size != position) {
				fc.position(size);
			}
			
			fc.write(ByteBuffer.wrap(msg.getBytes(charset)));
			fc.write(ByteBuffer.wrap(lineSeparator.getBytes()));

		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			if (fileLock != null) {
				try {
					fileLock.release();
				} catch (IOException e) {
				}
			}
		}

		try {
			if (fc.size() >= maxSize) {
				rollover();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void rollover() throws Exception {
		close();

		rollIndex++;
		file.renameTo(new File(filePath + "_" + rollIndex));

		file = new File(filePath);
		open();
	}

	@Override
	public void close() {
		if(outStream != null){
			try{
				outStream.close();
			}catch(Exception e){}
			outStream = null;
		}
	}

}
