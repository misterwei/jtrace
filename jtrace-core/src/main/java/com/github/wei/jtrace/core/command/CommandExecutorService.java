package com.github.wei.jtrace.core.command;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.beans.IBeanFactory;
import com.github.wei.jtrace.api.beans.IBeanFactoryAware;
import com.github.wei.jtrace.api.beans.IBeanPostProcessor;
import com.github.wei.jtrace.api.beans.IBeanProcessorChain;
import com.github.wei.jtrace.api.command.Argument;
import com.github.wei.jtrace.api.command.ICommand;
import com.github.wei.jtrace.api.command.ICommandDescriptor;
import com.github.wei.jtrace.api.command.ICommandExecutor;
import com.github.wei.jtrace.api.command.ICommandResult;
import com.github.wei.jtrace.api.exception.BeanProcessException;
import com.github.wei.jtrace.core.util.TypeConvertUtils;

@Bean(type=ICommandExecutor.class)
public class CommandExecutorService implements ICommandExecutor, IBeanFactoryAware{
	
	private List<ICommand> commands = new CopyOnWriteArrayList<ICommand>();
	
	public CommandExecutorService() {
		addCommand(new GetEnvInfoCommand());
		addCommand(helpCommand);
	}
	
	@Override
	public ICommandResult execute(String commandName, Object... args) {
		ICommand command = matchCommand(commandName);
		if(command == null){
			return new CommandResult(false, "command ["+commandName+"] not found");
		}
		
		return innerExecute(command, args);
	}
	
	@Override
	public ICommandResult execute(ICommandDescriptor commandDescriptor, Object... args) {
		if(commandDescriptor instanceof CommandDescriptor){
			CommandDescriptor commandDescriptor2 = (CommandDescriptor)commandDescriptor;
			return innerExecute(commandDescriptor2.getCommand(), args);
		}
		
		return execute(commandDescriptor.getName(), args);
	}
	
	
	private ICommandResult innerExecute(ICommand command, Object... args) {
		try{
			Argument[] argumentTypes = command.args();		
			checkAndFillArguments(argumentTypes, args);
			
			Serializable commandResult = command.execute(args);
			CommandResult result = new CommandResult(true);
			result.setResult(commandResult);
			
			return result;
		}catch(Exception e){
			CommandResult exResult = new CommandResult(false, e.getMessage());
			exResult.setResult(e);
			return exResult; 
		}
	}
	
	private void checkAndFillArguments(Argument[] argumentTypes, Object[] args) throws Exception{
		if(argumentTypes == null) {
			return;
		}
		if(args == null) {
			throw new IllegalArgumentException("The number of parameters is not equal");
		}
		if(argumentTypes.length > args.length) {
			throw new IllegalArgumentException("The number of parameters is not equal");
		}
		for(int i=0;i<argumentTypes.length;i++) {
			Object arg = args[i];
			Argument argumentType = argumentTypes[i];
			if(arg == null) {
				if(argumentType.isNecessary()) {
					throw new IllegalArgumentException("Argument "+ argumentType.getName() + " can not be null");
				}
				if(argumentType.getDefaultValue() != null) {
					args[i] = argumentType.getDefaultValue();
				}else {
					args[i] = TypeConvertUtils.defaultValue(argumentType.getType());
				}
				continue;
			}
			if(arg.getClass() != argumentType.getType()) {
				throw new IllegalArgumentException("Argument "+ argumentType.getName() + " error in type " + arg.getClass());
			}
		}
		
	}
	
	@Override
	public ICommandDescriptor findCommand(String commandName) {
		ICommand command = matchCommand(commandName);
		if(command == null){
			return null;
		}
		
		return new CommandDescriptor(command);
	}
	
	
	private ICommand matchCommand(String commandName) {
		for(ICommand command : commands){
			if(commandName.equals(command.name())){
				return command;
			}
		}
		
		return null;
	}
	
	private void addCommand(ICommand command){
		commands.add(command);
	}

	@Override
	public void setBeanFactory(IBeanFactory beanFactory) {
		beanFactory.registBeanPostProcessor(ICommand.class, new CommandBeanPostProcessor());		
	}
	
	private class CommandBeanPostProcessor implements IBeanPostProcessor{
		@Override
		public int priority() {
			return 0;
		}

		@Override
		public <T> T process(T obj, IBeanProcessorChain chain) throws BeanProcessException {
			addCommand((ICommand)obj);
			return obj;
		}
	}

	private ICommand helpCommand = new ICommand() {
		@Override
		public String name() {
			return "help";
		}
		
		@Override
		public String introduction() {
			return "help";
		}
		
		@Override
		public Serializable execute(Object... args) throws Exception {
			ArrayList<Map<String, Object> > result = new ArrayList<Map<String, Object> >();
			Object commandName = args[0];
			if(commandName == null){
				for(ICommand command : commands){
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("name", command.name());
					map.put("introduction", command.introduction());
					map.put("args", command.args());
					result.add(map);
				}
				
			}else{
				ICommand command = matchCommand(commandName.toString());
				if(command != null){
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("name", command.name());
					map.put("introduction", command.introduction());
					map.put("args", command.args());
					result.add(map);
				}
			}
			return result;
		}
		
		@Override
		public Argument[] args() {
			return new Argument[]{new Argument("command", "命令", false, String.class)};
		}
	};
	
	
}
