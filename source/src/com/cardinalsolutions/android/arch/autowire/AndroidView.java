package com.cardinalsolutions.android.arch.autowire;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to denote a field variable in an activity class that can be found by id at runtime.
 * @author Jacob Kanipe-Illig (jkanipe-illig@cardinalsolutions.com)
 * Copyright (c) 2013
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface AndroidView {
	/**
	 * You may optionally specify the id of the android view.
	 * If id is not set, assume the name of the field is the id.
	 * If the {@code value} is set, this field will be ignored
	 * @return id
	 */
	String id() default "";
	
	/**
	 * This View must be autowired. If required is true, then if the field cannot
	 * be autowired, and exception is thrown.  No exception is thrown and the
	 * autowire will fail silently if required is false.
	 * <br /><br />
	 * defaults to {@code true}
	 * @return
	 */
	boolean required() default true;
	
	/**
	 * Resource ID for the view. Example: {@code R.id.viewname}
	 * <br /><br />
	 * You may optionally specify this id of the android view.  If the value is set,
	 * this will be used to autowire the field.  If it is not set, the {@code id} variable will
	 * be used. If the {@code id} is not set, then the name of the field is the id.
	 * <br /><br />
	 * Using value is recommended as it is the most efficient way to autowire the view.
	 * @return
	 */
	int value() default 0;
}
