package com.github.wei.server;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.api.command.Argument;
import com.github.wei.jtrace.api.command.ICommandDescriptor;
import com.github.wei.jtrace.api.command.ICommandExecutor;
import com.github.wei.jtrace.api.command.ICommandResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.QueryStringDecoder;

public class CommandExecuteHandler extends ChannelInboundHandlerAdapter{
	static Logger log = LoggerFactory.getLogger(CommandExecuteHandler.class);
	private ICommandExecutor commandExecutor;
	
	@SuppressWarnings("rawtypes")
	private GsonBuilder gsonBuilder = new GsonBuilder().registerTypeAdapterFactory(TypeAdapters.newFactory(Class.class, new TypeAdapter<Class >() {
		@Override
		public void write(JsonWriter out, Class value) throws IOException {
			if (value == null) {
		         out.nullValue();
		         return;
		       }
			out.value(value.getName());
		}

		@Override
		public Class read(JsonReader reader) throws IOException {
			if (reader.peek() == JsonToken.NULL) {
		         reader.nextNull();
		         return null;
		       }
		       String className = reader.nextString();
		       
			try {
				return Class.forName(className);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			return null;
		}
	}));
	
	public CommandExecuteHandler(ICommandExecutor commandExecutor) {
		this.commandExecutor = commandExecutor;
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
		if (!(msg instanceof HttpRequest)) {
            return;
		}
		HttpRequest req = (HttpRequest) msg;
		
		if (HttpUtil.is100ContinueExpected(req)) {
            ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
        }
        boolean keepAlive = HttpUtil.isKeepAlive(req);

        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(req.uri());
        String command = queryStringDecoder.path().substring(1);
        
        ICommandResult result = null;
        try{
	        ICommandDescriptor commandDescriptor = commandExecutor.findCommand(command);
	        Object[] args = new Object[0];
	        if(commandDescriptor != null){
	        	Argument[] arguments = commandDescriptor.getArgs();
	        	if(arguments != null && arguments.length > 0){
	        		args = new Object[arguments.length];
		        	for(int i=0;i<arguments.length;i++){
		        		Argument argument =  arguments[i];
		        		List<String> param = queryStringDecoder.parameters().get(argument.getName());
		        		args[i] = extractParameter(param, argument);
		        	}
	        	}
	        }
	        result = commandExecutor.execute(command, args);
        }catch(IllegalArgumentException e){
        	String message = "command ("+command+") execute failed";
        	log.error(message, e);
        	result = new CommandFailedResult(command, e.getMessage());
        }
        
        //异常的堆栈需要显示调用getStackTrace()才可以赋值到stackTrace中
        Object r = result.getResult();
        if(r != null && r instanceof Throwable) {
        	Throwable t = (Throwable)r;
        	t.getStackTrace();
        	
        	while(t.getCause() != null) {
        		t = t.getCause();
        		t.getStackTrace();
        	}
        }
        
        Gson gson = gsonBuilder.create();
        
        ByteBuf content = ctx.alloc().buffer();
        content.writeBytes(gson.toJson(result).getBytes("utf-8"));
        
//        ByteBufUtil.writeAscii(content, " - via " + req.protocolVersion() + " (" + establishApproach + ")");

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK, content);
        response.headers().set("CONTENT-TYPE", "text/plain; charset=UTF-8");
        response.headers().setInt("CONTENT-LENGTH", response.content().readableBytes());

        if (!keepAlive) {
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set("CONNECTION", HttpHeaderValues.KEEP_ALIVE);
            ctx.write(response);
        }
    }
	
	private Object extractParameter(List<String> params, Argument arg) {
		if(params == null && arg.isNecessary()){
			throw new IllegalArgumentException("parameter ("+arg.getName()+")  must be set");
		}
		if(params == null){
			return arg.getDefaultValue();
		}
		
		Class<?> type = arg.getType();
		if(type.isArray()){
			Object array = Array.newInstance(type.getComponentType(), params.size());
			for(int i =0;i<params.size();i++){
				String param = params.get(i);
				Array.set(array, i, TypeConvertUtils.convert(param, type.getComponentType()));
			}
			return array;
		}else{
			if(params.size() > 0){
				String param = params.get(0);
				return TypeConvertUtils.convert(param, type);
			}
			return arg.getDefaultValue();
		}
	}

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
