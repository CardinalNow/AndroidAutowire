Android Autowire
======

Using Java Annotations and Reflection, this library will allow you to replace some of annoying boilerplate setup from your Activities, Fragments, and Views with an annotation based approach.

This repository is referenced in the blog post: http://www.cardinalsolutions.com/cardinal/blog/mobile/2014/01/dealing_with_android.html

Features
------

* Supports Inheritance of Activities. You can inherit views from parent Activities, and every view will be picked up and wired in
* As it uses reflection, it will work with private variables
* Comes with several out of the box ways of specifying IDs allowing for flexibility in naming IDs and implementing the annotations
* Provides an optional required field in the annotation, so if an ID is not found, the variable will be skipped without an Exception being thrown
* Support Annotations for Layout as well as Views
* Support an Annotation based approach for saving instance state.  This also allows for inheritance.
* Can be adapted to work with Fragments as well as Activities
* Can be adapted to work with CustomViews


The Android Way
---------

Here are some Examples of Android Boilerplate code that we can make more clear, readable, and easier to use with Annotations.

### findViewById()

One particularly jarring example of Android boilerplate code is the ```findViewById()``` method.  Every time you want to access an Android view defined in your XML, you need to use this method, often with a typecast.  For large Activities with many views, this can add a lot of code that does nothing but pull variables out of the xml.

```java
public class MainActivity extends BaseActivity{

	private ImageView logo;

	@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

    	logo = (ImageView) findViewById(R.id.logo);
	}
}
```

### setContentView()

In the code example above, we have the ```setContentView(R.layout.main)``` line.  You need something like this in every Activity class, with the sole purpose of inflating your layout.  It's not a big deal, but it is one extra step you have to go through when creating your Activity classes because it has to be put in exactly the right spot.  It needs to be in ```onCreate()``` before any ```findViewById()``` call.

### Saving Instance State

A quirk of how the Android operating systems works, Activities can be destroyed at almost anytime to make room for other OS processes.  They are also destroyed and re-created on rotation.  The developer is in charge of saving the Activity's state, making sure the Activity comes back exactly the same way before it was destroyed.

In the Android way, instance variables that you have to manually story are put into a ```Bundle``` in the ```onSaveInstanceState``` method.  Then they must be pulled out again in the ```onCreate()``` method.

```java
public class MainActivity extends BaseActivity{

    private static final String SOME_STATE_KEY = "some_state_key";
	private int someState;

	@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

    	if(savedInstanceState != null){
              someState = savedInstanceState.getInt(SOME_STATE_KEY);
        }
	}

    @Override
    protected void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
        outState.putInt(SOME_STATE_KEY, someState);    
    }
}
```

With AndroidAutowire
------------


This library will help streamline this process into a more readable format using annotations and reflection.  


### findViewById()
By annotating a class variable for the View with the ```@AndroidView``` custom annotation, you enable the reflection code to pull the view out of the xml.  The variable name will be the view id, or alternatively, the view id can be specified in the annotation.  The annotation processing occurs in an overridden method of ```setContentView(int layoutResID)``` in the Activity’s base class.


#### MainActivity Class

```java
public class MainActivity extends BaseActivity{

	@AndroidView
	private ImageView logo;

	@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
	}
}
```

#### BaseActivity class

```java
public class BaseActivity extends Activity {

	@Override
    public void setContentView(int layoutResID) {
    	super.setContentView(layoutResID);
    	AndroidAutowire.autowire(this, BaseActivity.class);
    }
}
```

### setContentView()

Specifying the layout resource in the onCreate is not difficult, but it can create problems if you forget add the method call, or if you do it out of order.  Instead, use an annotation:

#### MainActivity Class

```java
@AndroidLayout(R.layout.main)
public class MainActivity extends BaseActivity{

	@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
	}
}
```

#### BaseActivity class

```java
public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        int layoutId = AndroidAutowire.getLayoutResourceByAnnotation(this, this, BaseActivity.class);
		//If this activity is not annotated with AndroidLayout, do nothing
		if(layoutId == 0){
			return;
		}
		setContentView(layoutId);
    }

	@Override
    public void setContentView(int layoutResID) {
    	super.setContentView(layoutResID);
    	AndroidAutowire.autowire(this, BaseActivity.class);
    }
}
```

### Saving Instance State

All of the reading/writing with the Bundle can be done with reflection.  Simply annotate the instance variable you want to save/load, and the AndroidAutowire library will do the work for you.

#### MainActivity Class

```java
@AndroidLayout(R.layout.main)
public class MainActivity extends BaseActivity{
    @SaveInstance
    private int someState;

	@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
	}
}
```

#### BaseActivity Class

```java
public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        AndroidAutowire.loadFieldsFromBundle(savedInstanceState, this, BaseActivity.class);
    }

    @Override
	protected void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
		AndroidAutowire.saveFieldsToBundle(outState, this, BaseActivity.class);
	}
}
```

Configuration
-------

Simply include the jar in your classpath.  The process for including the AndroidAutowire library will be IDE specific, but once the library is included in the project, the methods will all be there for you to use.

You can create your own BaseActivity using the process above, or you can use a provided BaseActivity called ```BaseAutowireActivity```.  That will provide support for all features given above, as well as including a new abstract method that acts as a callback once the autowiring is complete. If you use features like ```BaseAutowireActivity``` and ```@AndroidLayout``` it may not even be necessary to override ```onCreate``` in your Activity class.

Fragments
---------

Much like Activities, Fragments have layouts, state to be saved, and views to be autowired. But the process for setting up a Fragment is different than an Activity.  None the less, AndroidAutowire provides the ability to do all of this using Annotations as well by providing a new method: ```AndroidAutowire.autowireFragment()```.

Here is an Example base class for Fragments:

```java
public abstract class BaseFragment extends Fragment {

	protected View contentView;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//Load any annotated fields from the bundle
		AndroidAutowire.loadFieldsFromBundle(savedInstanceState, this, BaseFragment.class);
		
		//Load the content view using the AndroidLayout annotation
        contentView = super.onCreateView(inflater, container, savedInstanceState);
        if (contentView == null) {
        	int layoutResource = AndroidAutowire.getLayoutResourceByAnnotation(this, getActivity(), BaseFragment.class);
        	if(layoutResource == 0){
            	return null;
            }
        	contentView = inflater.inflate(layoutResource, container, false);
        }
        //If we have the content view, autowire the Fragment's views
        autowireViews(contentView);
        //Callback for when autowiring is complete
        afterAutowire(savedInstanceState);
        return contentView;
    }
	
	protected void autowireViews(View contentView){
		AndroidAutowire.autowireFragment(this, BaseFragment.class, contentView, getActivity());
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
		AndroidAutowire.saveFieldsToBundle(outState, this, BaseFragment.class);
	}
	
	protected abstract void afterAutowire(Bundle savedInstanceState);
}
```

Unfortunately, do to fragmentation between the Android Core API and the Support Library, this class is not included with the Jar (whereas BaseAutowireActivity is included).

Custom Views
--------------

If you are writing a non-trivial Android App, chances are you will need to make your own custom Views at some point.  These views may have subviews.  Again, rather than being forced to use ```findViewById()```, we can use AndroidAutowire and Annotations with the ```AndroidAutowire.autowireView()``` method.

```java
public class CustomView extends RelativeLayout {

	@AndroidView(R.id.title)
	private TextView title;
	
	@AndroidView(R.id.icon)
	private ImageView icon;

    public CustomView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.custome_view, this);
		AndroidAutowire.autowireView(this, CustomView.class, context);
	}
}
```

Comparison to Other Libraries
-------

There are some other open source libraries that accomplish something similar to what Android Autowire hopes to provide

**RoboGuice** is a dependency injection library that can inject views in much the same way.  However, you must extend the Robo* classes, and there may be performance issues. (https://github.com/roboguice/roboguice/wiki)

**Android Annotations** can wire in views by annotation, but the approach they take is quite different.  Android Annotations requires you to use an extra compile step, creating generated Activity classes that must be referenced in the AndroidManifest.xml.  As this approach will create subclasses of your Activity, you cannot use this on private variables.  Additionally, there is much more configuration and initial setup. (https://github.com/excilys/androidannotations/wiki)

**Butter Knife** does the same compile time annotation approach as Android Annotations, but instead of generating a new Activity, they generate a class to pass your activity into. This way, you don't have to deal with generated sub classes, but you still get some of the heavy hitting features like onClick Listeners. (http://jakewharton.github.io/butterknife/)

The real advantage to this "Android Autowire" library is ease of use.  There is minimal configuration in just about every IDE, and little overhead, allowing you to quickly start using these annotations in your new or existing project.  Instead of providing a full feature set, this library concentrates only on limited number of features, such as views, layouts and Bundle resources, allowing it to fill the gap while still being lightweight.


Performance
------------

Reflection code is known to be a bit inefficient on Android. However, because this library is only looking at a small subset of the Activity fields, only the declared fields in each class extending from your base activity, performance is virtually the same as using ```findViewById()```, even on some sizable activities with plenty of class variables.

The more you use the library, the more you want to keep an eye out for performance hits. Most of this reflection code is going to be done on the main thread, and that is always a risk. However, I have been using all of the features, from loading Serializable objects from the Bundle to finding views inside of Fragments, and I have not noticed any type of performance decrease. In fact, even some very complex Activities have made full use of this reflection code without any issue. My biggest concern would be older devices that I have not tested on, devices that may be slow to begin with.

## Author / License

Copyright Cardinal Solutions 2013. Licensed under the MIT license.
<img src="https://raw.github.com/CardinalNow/NSURLConnection-Debug/master/logo_footer.png"/>


