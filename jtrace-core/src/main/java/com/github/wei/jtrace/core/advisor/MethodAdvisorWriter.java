package com.github.wei.jtrace.core.advisor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.core.util.Constants;

public class MethodAdvisorWriter extends AdviceAdapter implements Opcodes {
	static Logger logger = LoggerFactory.getLogger("MethodAdvisorWriter");
	
	// -- Lebel for try...catch block
	private final Label beginLabel = new Label();
	private final Label endLabel = new Label();
	
	protected final Type TYPE_ADVISORINVOKER = Type.getType("Lcom/github/wei/jtrace/agent/AdvisorInvoker;");
	protected final Type TYPE_IADVICE = Type.getType("Lcom/github/wei/jtrace/agent/IAdvice;");

	protected final Type TYPE_CLASS = Type.getType(Class.class);
	protected final Type TYPE_OBJECT = Type.getType(Object.class);
	protected final Type TYPE_OBJECT_ARRAY = Type.getType(Object[].class);
	protected final Type TYPE_STRING = Type.getType(String.class);
	protected final Type TYPE_INT = Type.getType(int.class);
	protected final Type TYPE_THROWABLE = Type.getType(Throwable.class);
	protected final Type TYPE_METHOD = Type.getType(java.lang.reflect.Method.class);
	protected final Method METHOD_METHOD_INVOKE = Method.getMethod("Object invoke(Object,Object[])");
	protected final Method METHOD_IADVICE_ONRETURN = Method.getMethod("void onReturn(Object)");
	protected final Method METHOD_IADVICE_ONTHROW = Method.getMethod("void onThrow(Throwable)");
	protected final Method METHOD_IADVICE_ONINVOKE = Method.getMethod("void onInvoke(Integer,String,String,String,boolean)");
	
	private Integer lineNumber;
	private int adviceLocalIndex = -1;
	private boolean writable = true;
	private final String CLASS_NAME;
	private String name, descr;
	private boolean trace;
	
	protected MethodAdvisorWriter(String className, MethodVisitor mv, int access, String name, String desc, boolean trace) {
		super(ASM5, mv, access, name, desc);
		this.CLASS_NAME = className;
		this.name = name;
		this.descr = desc;
		this.trace = trace;
	}

	/**
     * 是否静态方法
     * @return true:静态方法 / false:非静态方法
     */
    private boolean isStaticMethod() {
        return (methodAccess & ACC_STATIC) != 0;
    }

    /**
     * 是否抛出异常返回(通过字节码判断)
     * @param opcode 操作码
     * @return true:以抛异常形式返回 / false:非抛异常形式返回(return)
     */
    private boolean isThrow(int opcode) {
        return opcode == ATHROW;
    }

    /**
     * 将NULL推入堆栈
     */
    protected void pushNull() {
        push((Type) null);
    }

    /**
     * 加载this/null
     */
    protected void loadThisOrPushNullIfIsStatic() {
        if (isStaticMethod()) {
            pushNull();
        } else {
            loadThis();
        }
    }
	
    protected void pushClass(String className) {
    	if (isStaticMethod()) {
    		push(className.replace('/', '.'));
    		invokeStatic(TYPE_CLASS, Method.getMethod("Class forName(String)"));
    	}else {
    		visitLdcInsn(Type.getType('L'+className+";.class"));
    	}
    }
    
    private void loadAdvisorInvokerMethod() {
		getStatic(TYPE_ADVISORINVOKER, "ON_BEFORE_METHOD", TYPE_METHOD);
			
    }
    
    private void loadInvokeArgs() {
    	push(5);
    	newArray(TYPE_OBJECT);
    	
    	dup();
    	push(0);
    	//获取当前类
		pushClass(CLASS_NAME);
		arrayStore(TYPE_CLASS);
		
		dup();
		push(1);
		//This对象
		loadThisOrPushNullIfIsStatic();
		arrayStore(TYPE_OBJECT);
		
		dup();
		push(2);
		//方法名
		push(name);
		arrayStore(TYPE_STRING);
		
		dup();
		push(3);
		//方法描述
		push(descr);
		arrayStore(TYPE_STRING);
		
		dup();
		push(4);
		//方法参数
		loadArgArray();
		arrayStore(TYPE_OBJECT_ARRAY);
    }
    
    
    private void loadAdviceAndReturn(int opcode) {
        switch (opcode) {

            case RETURN: {
            	loadLocal(adviceLocalIndex);
                pushNull();
                break;
            }

            case ARETURN: {
                dup();
                loadLocal(adviceLocalIndex);
                swap();
                break;
            }

            case LRETURN:
            case DRETURN: {
                dup2();
                box(Type.getReturnType(methodDesc));
                loadLocal(adviceLocalIndex);
                swap();
                break;
            }

            default: {
            	dup();
                box(Type.getReturnType(methodDesc));
                loadLocal(adviceLocalIndex);
                swap();
                break;
            }

        }
    }
    
    /**
     * 异常副本
     */
    private void loadAdviceAndThrow() {
    	dup();
    	loadLocal(adviceLocalIndex);
    	swap();
    }
    
    protected void markWeaved(String weavedTag) {
    	super.visitAnnotation(weavedTag, false);
    }
    
	@Override
	protected void onMethodEnter() {
		if(!writable) {
			return;
		}
		
		markWeaved(Constants.ADVISOR_WEAVED_CLASS);
		
		if(trace) {
			markWeaved(Constants.TRACER_WEAVED_CLASS);
		}
		
		loadAdvisorInvokerMethod();
		
		//调用静态方法AdvisorInvoker.onMethonBegin(),第一个参数为NULL
		pushNull();
		
		loadInvokeArgs();
		
		// 调用方法, 返回IAdvice
        invokeVirtual(TYPE_METHOD, METHOD_METHOD_INVOKE);
        
        checkCast(TYPE_IADVICE);
        
        adviceLocalIndex = newLocal(TYPE_IADVICE);
        storeLocal(adviceLocalIndex);
        
        mark(beginLabel);
        
        if(logger.isDebugEnabled()) {
        	logger.debug("transform method {}.{}{} enter, advice var index:{}", CLASS_NAME, name, descr, adviceLocalIndex);
        }
	}
	
	@Override
	protected void onMethodExit(int opcode) {
		if(!writable) {
			return;
		}
		
		if(!isThrow(opcode)) {
			loadAdviceAndReturn(opcode);
			
			//调用方法
            invokeInterface(TYPE_IADVICE, METHOD_IADVICE_ONRETURN);
            
            if(logger.isDebugEnabled()) {
            	logger.debug("transform method {}.{}{} exit", CLASS_NAME, name, descr);
            }
		}
	}
	
	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		if(writable) {
			mark(endLabel);
			
			visitTryCatchBlock(beginLabel, endLabel, mark(), TYPE_THROWABLE.getInternalName());
            // catchException(beginLabel, endLabel, ASM_TYPE_THROWABLE);
			
			loadAdviceAndThrow();
			
			invokeInterface(TYPE_IADVICE, METHOD_IADVICE_ONTHROW);
			
            throwException();
		}
        super.visitMaxs(maxStack, maxLocals);
	}
	
	@Override
	public void visitLineNumber(int line, Label start) {
		if(writable && trace) {
			this.lineNumber = line;
		}
		super.visitLineNumber(line, start);
	}
	
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
		if(writable && trace && adviceLocalIndex != -1) {
			if(logger.isDebugEnabled()) {
				logger.debug("transform method {}.{}{} invoke {}.{}{}, advice var index:{}", CLASS_NAME, this.name, descr, owner, name, desc, adviceLocalIndex);
			}
			loadLocal(adviceLocalIndex);
			if(lineNumber != null) {
				push(lineNumber);
				box(TYPE_INT);
			}else {
				pushNull();
			}
			push(owner);
			push(name);
			push(desc);
			push(itf);
			invokeInterface(TYPE_IADVICE, METHOD_IADVICE_ONINVOKE);
		}else {
			if(logger.isDebugEnabled()) {
				logger.debug("transform method {}.{}{} invoke {}.{}{}, advice var index:{}, skiped.", CLASS_NAME, this.name, descr, owner, name, desc, adviceLocalIndex);
			}
		}
		super.visitMethodInsn(opcode, owner, name, desc, itf);
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if(Constants.ADVISOR_WEAVED_CLASS.equals(desc)) {
			logger.info("Transform advisor class {} method {}{} will be skiped, It has been written", CLASS_NAME, name, descr);
			writable = false;
		}
		return super.visitAnnotation(desc, visible);
	}
}
