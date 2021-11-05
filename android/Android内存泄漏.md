# Android内存泄漏相关

> 总结来自：[https://blog.csdn.net/m0_37700275/article/details/77266565](https://blog.csdn.net/m0_37700275/article/details/77266565)

目录

- [1.错误使用单例造成的内存泄漏](#1.错误使用单例造成的内存泄漏)
- [2.错误使用静态变量，导致引用后无法销毁](#2.错误使用静态变量，导致引用后无法销毁)
- [3.Handler使用不当造成的内存泄漏](#3.Handler使用不当造成的内存泄漏)
- [4.不需要用的监听未移除会发生内存泄露](#4.不需要用的监听未移除会发生内存泄露)
- [5.广播注册之后没有被销毁导致内存泄漏](#5.广播注册之后没有被销毁导致内存泄漏)
- [6.动画资源未释放导致内存泄漏](#6.动画资源未释放导致内存泄漏)
- [7.系统bug之InputMethodManager导致内存泄漏](#7.系统bug之InputMethodManager导致内存泄漏)
- [8.资源未关闭导致资源被占用而内存泄漏](#8.资源未关闭导致资源被占用而内存泄漏)


### <a name="1.错误使用单例造成的内存泄漏">1.错误使用单例造成的内存泄漏</a>

```
public class LoginManager {
    private static LoginManager mInstance;
    private Context mContext;

    private LoginManager(Context context) {
        this.mContext = context;
    }

    public static LoginManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (LoginManager.class) {
                if (mInstance == null) {
                    mInstance = new LoginManager(context);
                }
            }
        }
        return mInstance;
    }
}
```

- 如果Context生命周期比LoginManager生命周期短时，就会导致Context无法被释放回收，从而造成内存泄漏。
- 解决办法：修改代码 this.mContext = context.getApplicationContext();

### <a name="2.错误使用静态变量，导致引用后无法销毁">2.错误使用静态变量，导致引用后无法销毁</a>

```
public class MyActivity extends AppCompatActivity {
    public static InnerClass innerClass = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        innerClass = new InnerClass();
    }

    class InnerClass {
        public void doSomeThing() {}
    }
}
```

- java中非静态内部类会隐式持有外部类的引用。当MyActivity在onCreate方法中调用了new InnerClass()时，innerClass就隐式持有了MyActivity的引用。而innerClass是静态类型，所以不会被垃圾回收，从而MyActivity也无法被垃圾回收，因此造成了内存泄露。

```
public class MainActivity extends AppCompatActivity {
    private static TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = new TextView(this);
    }
}
```	

- 静态变量引用不当会导致内存泄漏。textView持有了MainActivity.this的引用，而textView被定义成了static静态从而不会被垃圾回收，因此MainActivity资源无法被释放，造成内存泄漏。

### <a name="3.Handler使用不当造成的内存泄漏">3.Handler使用不当造成的内存泄漏</a>

1）Handler内存泄漏代码：

```
public class MainActivity extends AppCompatActivity {
    private Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = (TextView) findViewById(R.id.text);
        
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                textView.setText("hello");
            }
        }, 2000);
    }
}
```

- 什么是内部类：
	- 可以将一个类的定义放在里另一个类的内部，这就是内部类。广义上我们将内部类分为四种：成员内部类、静态内部类、局部（方法）内部类、匿名内部类。
- ActivityThread介绍：
	- 当APP进程创建时，会自动创建一个ActivityThread主线程。
	- ActivityThread的main方法中Looper.prepareMainLooper()会自动创建一个Looper对象。Looper创建时又会自动关联一个MessageQueue对象。
	- ActivityThread的main方法中Looper.loop()会不断地在MessageQueue进行消息轮询处理（阻塞式死循环）。
- 造成内存泄漏原因分析：
	- `new Runnable(){}`是匿名内部类，内部类会隐式持有外部类的引用（也就是`new Runnable(){}`持有MainActivity.this引用）。
	- 当Handler执行postDelayed方法时，内部会创建一个Message对象，并把`new Runnable(){}`放到Message中（所以Message持有MainActivity.this引用）
	- 当在ActivityThread中`new Handler()`时，会自动获取ActivityThread的MessageQueue对象。
	- 当调用Handler的postDelayed方法的实质，是将Message放入到ActivityThread的MessageQueue中处理。
	- 所以当退出Activity时MessageQueue中有未处理的Message时，就会导致Activity无法及时回收，从而引发内存泄漏。
- 引用链如下：
	- Message的`new Runnable(){}` 持有 MainActivity（也就是Message持有MainActivity）
	- MessageQueue 持有 Message
	- ActivityThread 持有 MessageQueue
	- 所以当持有MainActivity的Message未处理完成时退出MainActivity，就会导致内存泄漏。

2）解决方案一：

```
@Override
protected void onDestroy() {
	if(handler!=null){
        handler.removeCallbacksAndMessages(null);
        handler = null;
    }
    super.onDestroy();
}
```

- 要想避免Handler引起内存泄漏问题，需要我们在Activity退出时移除所有消息和消息中的Runnable。
- 所以只需在onDestroy()函数中调用mHandler.removeCallbacksAndMessages(null)即可。

3）解决方案二：

```
public class MainActivity extends AppCompatActivity {
	//静态内部类不会引用外部类
	private static class MyHandler extends Handler {
		//弱引用避免对象无法被垃圾回收
	    private final WeakReference<Context> mReference;
	
	    MyHandler(Context context) {
	        mReference = new WeakReference<>(context);
	    }
	
	    @Override
	    public void handleMessage(Message msg) {
	        super.handleMessage(msg);
	        Context context = mReference.get();
	        if (context == null) {
	            return;
	        }
	        int what = msg.what;
	        Object obj = msg.obj;
	        String txt = "what = " + what + ", obj = " + String.valueOf(obj);
	        Toast.makeText(context, txt, Toast.LENGTH_SHORT).show();
	    }
	}
	
	//静态内部类不会引用外部类
	private static class MyRunnable implements Runnable {
	    @Override
	    public void run() {
	    	Log.d("", "Message.Runnable run()")
	    }
	}
	
	private MyHandler mHandler = new MyHandler(this);
	private MyRunnable mRunnable = new MyRunnable(this);
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_main);
	    
	    //此时不会出现内存泄漏
	    mHandler.postDelayed(mRunnable, 2000);
	    finsh();
	}
}    
```

- 使用 "静态内部类" + "弱引用" 解决handler内存泄漏问题
- 静态内部类不会引用外部类
- 弱引用能避免对象被强引用

### <a name="4.不需要用的监听未移除会发生内存泄露">4.不需要用的监听未移除会发生内存泄露</a>

问题代码：

```
//add监听，放到集合里面
tv.getViewTreeObserver().addOnWindowFocusChangeListener(new ViewTreeObserver.OnWindowFocusChangeListener() {
    @Override
    public void onWindowFocusChanged(boolean b) {
    
    }
});
```

解决办法：

```
//计算完后，一定要移除这个监听
tv.getViewTreeObserver().removeOnWindowFocusChangeListener(this);
```

注意事项：

```
//监听执行完回收对象，不用考虑内存泄漏
tv.setOnClickListener(...);
//add监听，放到集合里面，需要考虑内存泄漏
tv.getViewTreeObserver().addOnWindowFocusChangeListener(...)
```

### <a name="5.广播注册之后没有被销毁导致内存泄漏">5.广播注册之后没有被销毁导致内存泄漏</a>

```
public class MeAboutActivity extends BaseActivity {

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //注册广播
        this.registerReceiver(mReceiver, new IntentFilter());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消注册广播，避免内存泄漏
        this.unregisterReceiver(mReceiver);
    }
}
```

- 在Activity中注册广播，如果不取消注册，那么这个广播会一直存在系统中。而非静态内部类的广播持有Activity引用，从而导致Activity无法释放，发生内存泄露。

### <a name="6.动画资源未释放导致内存泄漏">6.动画资源未释放导致内存泄漏</a>

```
public class LeakActivity extends AppCompatActivity {
	ObjectAnimator objectAnimator;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_leak);
		TextView textView = (TextView)findViewById(R.id.text_view);
		    
		ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(textView,"rotation",0,360);
		//动画无限循环
		objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
		objectAnimator.start();
	}
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    //取消动画
	    objectAnimator.cancel();
	}
}
```

- 在属性动画中有一类无限循环动画，如果在Activity的onDestroy中没有去停止动画，那么这个动画将会一直播放下去。由于Activity会被View所持有，从而导致Activity无法被释放。

### <a name="7.系统bug之InputMethodManager导致内存泄漏">7.系统bug之InputMethodManager导致内存泄漏</a>

- 每次从MainActivity退出程序时总会报InputMethodManager内存泄漏，原因系统中的InputMethodManager持有当前MainActivity的引用，导致了MainActivity不能被系统回收，从而导致了MainActivity的内存泄漏。查了很多资料，发现这是 Android SDK中输入法的一个Bug，在15<=API<=23中都存在，目前Google还没有解决这个Bug。

### <a name="8.资源未关闭导致资源被占用而内存泄漏">8.资源未关闭导致资源被占用而内存泄漏</a>

- 在使用IO、File流或者Sqlite、Cursor等资源时要及时关闭。
- 这些资源在进行读写操作时通常都使用了缓冲，如果及时不关闭，这些缓冲对象就会一直被占用而得不到释放，以致发生内存泄露。
