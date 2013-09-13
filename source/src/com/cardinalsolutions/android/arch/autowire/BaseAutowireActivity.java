package com.cardinalsolutions.android.arch.autowire;

import android.app.Activity;
import android.os.Bundle;

/**
 * Provided BaseActivity for use of AndroidAutowire annotations. <br /><br />
 * Use of this class means that you do not need to provide your own custom BaseActivity to
 * integrate with the AndroidAutowire library.
 * @author Jacob Kanipe-Illig (jkanipe-illig@cardinalsolutions.com)
 * Copyright (c) 2013
 */
public abstract class BaseAutowireActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		int layoutId = AndroidAutowire.getLayoutResourceByAnnotation(this, BaseAutowireActivity.class);
		//If this activity is not annotated with AndroidLayout, do nothing
		if(layoutId == 0){
			return;
		}
		setContentView(layoutId);
		afterAutowire(savedInstanceState);
	}
	
	@Override
	public void setContentView(int layoutResID){
		super.setContentView(layoutResID);
		//autowire the AndroidView fields
		AndroidAutowire.autowire(this, BaseAutowireActivity.class);
	}
	
	/**
	 * By default this method implementation is empty.
	 * <br /><br />
	 * This method will be called after views are autowired by AndroidAutowire
	 * and after the layout is created. This method will only be called when the
	 * AndroidLayout annotation is used to load the layout resource for the Activity.
	 * <br /><br />
	 * This method can be used as a substitute for {@code onCreate()}, as actually overriding
	 * {@code onCreate()} is not necessary when this base class does it for you. Activity set up
	 * that is usually done in {@code onCreate()} can be done in this method instead.
	 */
	protected abstract void afterAutowire(Bundle savedInstanceState);
}
