package com.github.wei.jtrace.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.command.ICommandExecutor;
import com.github.wei.jtrace.api.config.IConfig;
import com.github.wei.jtrace.api.service.IAsyncService;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

@Bean
public class NettyRestServer implements IAsyncService{
	
	Logger log = LoggerFactory.getLogger("NettyRestServer");
	
	@AutoRef
	private ICommandExecutor commandExecutor;
	
	private boolean running = false;
	
	private int port = 8888;
	
	private Channel channel;
	
	public String getId() {
		return "netty_server";
	}

	public boolean start(IConfig config) {
		log.info("Prepare start...");
		port = config.getInt("port", 8888);
		return true;
	}

	public void stop() {
		this.running = false;
		if(channel != null) {
			channel.close();
		}
	}

	public boolean isRunning() {
		return running;
	}

	public void run() {
		running = true;
		log.info("Starting, listen port {}", port);
		 // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(new ChannelInitializer<Channel>() {
				@Override
				protected void initChannel(Channel ch) throws Exception {
					ChannelPipeline p = ch.pipeline();
					p.addLast(new HttpServerCodec());
					p.addLast(new CommandExecuteHandler(commandExecutor));
				}
			});

            channel = b.bind(port).sync().channel();

            log.info("Open your web browser and navigate to http://127.0.0.1:" + port + '/');

            channel.closeFuture().sync();
        } catch(Exception e){
        	
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
		
        log.info("Stoped ");
        
        running = false;
	}

}
