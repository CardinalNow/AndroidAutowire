package com.cardinalsolutions.android.arch.autowire;

import java.io.Serializable;
import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * Annotation handler class that will wire in the android views at runtime.
 * <br /><br />
 * This class will look for the {@code @AndroidView} annotation in the activity class.
 * <br /><br />
 * <strong>Example Usage:</strong>
 * <br /><br />
 * Base Class for Activity
 * <pre class="prettyprint">
 * public class BaseActivity extends Activity {
 * 	...
 * 	{@code @Override}
 *	public void setContentView(int layoutResID){
 *		super.setContentView(layoutResID);
 *		AndroidAutowire.autowire(this, BaseActivity.class);
 *	}
 * }
 * </pre>
 * Activity Class
 * <pre class="prettyprint">
 * public class MainActivity extends BaseActivity{
 * 	{@code @AndroidView}
 * 	private Button main_button;
 * 
 * 	{@code @AndroidView(id="edit_text_field")}
 * 	private EditText editText;
 * 
 * 	{@code @AndroidView(value=R.id.img_logo, required=false)}
 * 	private ImageView logo;
 * 
 * 	{@code @Override}
 *	protected void onCreate(Bundle savedInstanceState) {		
 *		super.onCreate(savedInstanceState);
 *		setContentView(R.layout.activity_main)
 * 	}
 * }
 * </pre>
 * The layout xml :
 * <pre class="prettyprint">
 *  
 * &lt;EditText
 *    android:id="@+id/edit_text_field"
 *    android:layout_width="fill_parent" 
 *    android:layout_height="wrap_content"
 *    android:inputType="textUri" 
 *    /&gt;
 *  
 * &lt;Button  
 *    android:id="@+id/main_button"
 *    android:layout_width="fill_parent" 
 *    android:layout_height="wrap_content" 
 *    android:text="@string/test"
 *    /&gt;
 *    
 * &lt;ImageView  
 *    android:id="@+id/img_logo"
 *    android:layout_width="fill_parent" 
 *    android:layout_height="wrap_content" 
 *    android:text="@string/hello"
 *   /&gt;
 * </pre>
 * @author Jacob Kanipe-Illig (jkanipe-illig@cardinalsolutions.com)
 * Copyright (c) 2013
 */
public class AndroidAutowire {

	/**
	 * Perform the wiring of the Android View using the {@link AndroidView} annotation.
	 * <br /><br />
	 * <strong>Usage:</strong><br /><br />
	 * Annotation all view fields in the activity to be autowired.  Use {@code @AndroidView}.<br />
	 * If you do not specify the {@code id} or the {@code value} parameters in the annotation, the name of the variable will be used as the id.
	 * <br />
	 * You may specify whether or not the field is required (true by default).
	 * <br /><br />
	 * After the call to {@code setContentView(layoutResID)} in the onCreate() method, you will call this 
	 * {@code autowire(Activity thisClass, Class<?> baseClass)} method.
	 * <br />
	 * The first parameter is the Activity class being loaded.  <br />
	 * The second parameter is the class of the base activity (if applicable).
	 *
	 * @param thisClass The activity being created.
	 * @param baseClass The Base activity. If there is inheritance in the activities, this is the highest level, the base activity,
	 * but not {@link Activity}. <br /><br />All views annotated with {@code @AndroidView} will be autowired in all Activity classes in the 
	 * inheritance structure, from thisClass to baseClass inclusive. baseClass should not be {@link Activity} because no fields
	 * in {@link Activity} will need to be autowired. <br /><br /> If there is no parent class for your activity, use thisClass.class as baseClass.
	 * @throws AndroidAutowireException Indicates that there was an issue autowiring a view to an annotated field. Will not be thrown if required=false
	 * on the {@link AndroidView} annotation.
	 */
	public static void autowire(Activity thisClass, Class<?> baseClass) throws AndroidAutowireException{
		Class<?> clazz = thisClass.getClass();
		autowireViewsForClass(thisClass, clazz);
		//Do this for all classes in the inheritance chain, until we get to the base class
		while(baseClass.isAssignableFrom(clazz.getSuperclass())){
			clazz = clazz.getSuperclass();
			autowireViewsForClass(thisClass, clazz);
		}
	}
	
	/**
	 * Gets the layout resource id based on the Activity or Fragment. This class (or a parent class) must be annotated with the
	 * {@link AndroidLayout} annotation, or a valid layout id will not be returned.  This will work with Activity,
	 * Android core Fragment, and Android Support Library Fragment.
	 * @param thisClass Annotated class with the layout. This is generally the Activity or Fragment class
	 * @param thisActivity Context for the Activity or Fragment that is being laid out.
	 * @param the base activity/fragment allowing inheritance of layout
	 * @return layout id for the layout of this activity/fragment. If no layout resource is found, or if there is 
	 * no annotation for AndroidLayout present, then 0 is returned.
	 */
	public static int getLayoutResourceByAnnotation(Object thisClass, Context thisActivity, Class<?> baseClass) {
		AndroidLayout layoutAnnotation = thisClass.getClass().getAnnotation(AndroidLayout.class);
		Class<?> clazz = thisClass.getClass();
		while(layoutAnnotation == null && baseClass.isAssignableFrom(clazz.getSuperclass())){
			clazz = clazz.getSuperclass();
			layoutAnnotation = clazz.getAnnotation(AndroidLayout.class);
		}
		if(layoutAnnotation == null){
			return 0;
		}
		if(layoutAnnotation.value() != 0){
			return layoutAnnotation.value();
		}
		String className = thisClass.getClass().getSimpleName();
		int layoutId = thisActivity.getResources().getIdentifier(className, "layout", thisActivity.getPackageName());
		return layoutId;
	}
	
	/**
	 * Find all the fields (class variables) in the Activity/Fragment, and the base classes, that are annotated
	 * with the {@link SaveInstance} annotation.  These will be put in the Bundle object.
	 * @param bundle {@link Bundle} to save the Activity/Fragment's state
	 * @param thisClass Class with values being saved
	 * @param baseClass Bass class of the Activity or Fragment
	 */
	public static void saveFieldsToBundle(Bundle bundle, Object thisClass, Class<?> baseClass){
		Class<?> clazz = thisClass.getClass();
		while(baseClass.isAssignableFrom(clazz)){
			for(Field field : clazz.getDeclaredFields()){
				if(field.isAnnotationPresent(SaveInstance.class)){
					field.setAccessible(true);
					try {
						bundle.putSerializable(clazz.getName() + field.getName(), (Serializable) field.get(thisClass));
					} 
					catch (ClassCastException e){
						Log.w("AndroidAutowire", "The field \"" + field.getName() + "\" was not saved and may not be Serializable.");
					}
					catch (Exception e){
						//Could not put this field in the bundle.
						Log.w("AndroidAutowire", "The field \"" + field.getName() + "\" was not added to the bundle");
					}
				}
			}
			clazz = clazz.getSuperclass();
		}
	}
	
	/**
	 * Look through the Activity/Fragment and the Base Classes.  Find all fields (member variables)  annotated
	 * with the {@link SaveInstance} annotation. Get the saved value for these fields from the Bundle, and 
	 * load the value into the field.
	 * @param bundle {@link Bundle} with the Activity/Fragment's saved state.
	 * @param thisClass Activity/Fragment being re-loaded
	 * @param baseClass Base class of the Activity/Fragment
	 */
	public static void loadFieldsFromBundle(Bundle bundle, Object thisClass, Class<?> baseClass){
		if(bundle == null){
			return;
		}
		Class<?> clazz = thisClass.getClass();
		while(baseClass.isAssignableFrom(clazz)){
			for(Field field : clazz.getDeclaredFields()){
				if(field.isAnnotationPresent(SaveInstance.class)){
					field.setAccessible(true);
					try {
						Object fieldVal = bundle.get(clazz.getName() + field.getName());
						if(fieldVal != null){
							field.set(thisClass, fieldVal);							
						}
					} catch (Exception e){
						//Could not get this field from the bundle.
						Log.w("AndroidAutowire", "The field \"" + field.getName() + "\" was not retrieved from the bundle");
					}
				}
			}
			clazz = clazz.getSuperclass();
		}
	}
	
	/**
	 * Autowire views for a fragment.  This method works in as similar way to {@code autowire(Activity thisClass, Class<?> baseClass)}
	 * but for a Fragment instead of Activity. This will work with both an Android Fragment a Support Library Fragment.
	 * 
	 * @param thisClass This fragment class.  The type is Object to work around android backwards compatibility 
	 * with the API, as Fragment can come from the core API or from the support library.
	 * @param baseClass The Fragment's base class.  Allows inherited views.
	 * @param contentView The Fragment's main content view
	 * @param context Context for the fragment's activity. Generally, this should be {@code getActivity()}
	 * @throws AndroidAutowireException Indicates that there was an issue autowiring a view to an annotated field. Will not be thrown if required=false
	 * on the {@link AndroidView} annotation.
	 */
	public static void autowireFragment(Object thisClass, Class<?> baseClass, View contentView, Context context) throws AndroidAutowireException{
		Class<?> clazz = thisClass.getClass();
		autowireViewsForFragment(thisClass, clazz, contentView, context);
		//Do this for all classes in the inheritance chain, until we get to this class
		while(baseClass.isAssignableFrom(clazz.getSuperclass())){
			clazz = clazz.getSuperclass();
			autowireViewsForFragment(thisClass, clazz, contentView, context);
		}
	}
	
	/**
	 * Autowire a custom view class. Load the sub views for the custom view using the {@link AndroidView} annotation.
	 * Inheritance structures are supported.
	 * @param thisClass This Android View class to be autowired.
	 * @param baseClass The views parent, allowing inherited views to be autowired, if necessary. If there is no custom
	 * base class, just use this custom view's class.
	 * @param context Context
	 * @throws AndroidAutowireException Indicates that there was an issue autowiring a view to an annotated field. 
	 * Will not be thrown if required=false on the {@link AndroidView} annotation.
	 */
	public static void autowireView(View thisClass, Class<?> baseClass, Context context) throws AndroidAutowireException{
		autowireFragment(thisClass, baseClass, thisClass, context);
	}
	
	private static void autowireViewsForFragment(Object thisFragment, Class<?> clazz, View contentView, Context context){
		for (Field field : clazz.getDeclaredFields()){
			if(!field.isAnnotationPresent(AndroidView.class)){
				continue;
			}
			if(!View.class.isAssignableFrom(field.getType())){
				continue;
			}
			AndroidView androidView = field.getAnnotation(AndroidView.class);
			int resId = androidView.value();
			if(resId == 0){
				String viewId = androidView.id();
				if(androidView.id().equals("")){
					viewId = field.getName();
				}
				resId = context.getResources().getIdentifier(viewId, "id", context.getPackageName());			
			}
			try {
				View view = contentView.findViewById(resId);
				if(view == null){
					if(!androidView.required()){
						continue;
					}else{
						throw new AndroidAutowireException("No view resource with the id of " + resId + " found. "
								+" The required field " + field.getName() + " could not be autowired" );	
					}
				}
				field.setAccessible(true);
				field.set(thisFragment,view);
			} catch (Exception e){
				if(e instanceof AndroidAutowireException){
					throw (AndroidAutowireException) e;
				}
				throw new AndroidAutowireException("Cound not Autowire AndroidView: " + field.getName() + ". " + e.getMessage());
			}
		}
	}
	
	private static void autowireViewsForClass(Activity thisActivity, Class<?> clazz){
		for (Field field : clazz.getDeclaredFields()){
			if(!field.isAnnotationPresent(AndroidView.class)){
				continue;
			}
			if(!View.class.isAssignableFrom(field.getType())){
				continue;
			}
			AndroidView androidView = field.getAnnotation(AndroidView.class);
			int resId = androidView.value();
			if(resId == 0){
				String viewId = androidView.id();
				if(androidView.id().equals("")){
					viewId = field.getName();
				}
				resId = thisActivity.getResources().getIdentifier(viewId, "id", thisActivity.getPackageName());			
			}
			try {
				View view = thisActivity.findViewById(resId);
				if(view == null){
					if(!androidView.required()){
						continue;
					}else{
						throw new AndroidAutowireException("No view resource with the id of " + resId + " found. "
								+" The required field " + field.getName() + " could not be autowired" );	
					}
				}
				field.setAccessible(true);
				field.set(thisActivity,view);
			} catch (Exception e){
				if(e instanceof AndroidAutowireException){
					throw (AndroidAutowireException) e;
				}
				throw new AndroidAutowireException("Cound not Autowire AndroidView: " + field.getName() + ". " + e.getMessage());
			}
		}
	}
}
