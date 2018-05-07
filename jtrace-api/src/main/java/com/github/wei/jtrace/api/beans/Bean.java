package com.github.wei.jtrace.api.beans;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
public @interface Bean {
	String name() default "";
	Class<?>[] type() default Object.class;
}
