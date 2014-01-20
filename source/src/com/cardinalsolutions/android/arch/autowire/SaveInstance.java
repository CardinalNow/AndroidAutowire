package com.cardinalsolutions.android.arch.autowire;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to denote that a field should be saved into the Bundle {@code onSaveInstanceState()}.
 * The actual saving should be done using the {@link AndroidAutowire} library, but that library will
 * use this annotation to see what it should save and re-populate from the Bundle in {@code onCreate()}
 * @author Jacob Kanipe-Illig (jkanipe-illig@cardinalsolutions.com)
 * Copyright (c) 2013
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface SaveInstance {

}
