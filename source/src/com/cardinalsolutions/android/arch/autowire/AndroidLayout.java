package com.cardinalsolutions.android.arch.autowire;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to denote the layout resource for an activity class that can be found by layout id at runtime.
 * @author Jacob Kanipe-Illig (jkanipe-illig@cardinalsolutions.com)
 * Copyright (c) 2013
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface AndroidLayout {
	/**
	 * The value expects the layout resource id to be used for the Activity's layout.
	 * This is the layout id specified in {@code setConentView()}, which will not be
	 * needed in your activity if this annotation is used.
	 * <br /><br />
	 * If the value is not specified, the annotation will assume the resource name is 
	 * {@code R.layout.ThisActivityName} (assuming the annotated class has the class name: 
	 * {@code ThisActivityName}, and an exception will be thrown if the layout resource cannot be found. 
	 * @return value
	 */
	int value() default 0;
}
