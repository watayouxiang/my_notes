# Handler介绍

> ActivityThread,Handler,Looper,MessageQueue,Message 如何实现线程间通讯？
> 
> 参考自：[消息机制](https://github.com/yangchong211/YCBlogs/tree/master/android/%E6%B6%88%E6%81%AF%E6%9C%BA%E5%88%B6)



## <a name="第一篇">第一篇</a>

### <a name="1.为什么主线程中可以直接使用Handler">1.为什么主线程中可以直接使用Handler</a>

- App 初始化时会执行 ActivityThread 类的 main 方法。
- 查看 ActivityThread 源码：

	```
	Looper.prepareMainLooper();
	...
	Looper.loop();
	```

- 可以知道，主线程之所以可以使用 Handler 是因为在主线程已经初始化过Loop了。

### <a name="2.为什么每个线程只能创建一个Looper">2.为什么每个线程只能创建一个Looper</a>

```
static final ThreadLocal<Looper> sThreadLocal = new ThreadLocal<Looper>();

private static void prepare(boolean quitAllowed) {
    if (sThreadLocal.get() != null) {
        throw new RuntimeException("Only one Looper may be created per thread");
    }
    sThreadLocal.set(new Looper(quitAllowed));
}
```

- 可以看到Looper中有一个ThreadLocal成员变量，熟悉JDK的同学应该知道，当使用ThreadLocal维护变量时，ThreadLocal为每个使用该变量的线程提供独立的变量副本，所以每一个线程都可以独立地改变自己的副本，而不会影响其它线程所对应的副本。
- `Only one Looper may be created per thread` 说明：每个线程只能创建一个Looper，也就是说Looper.prepare()只能调用一次。
- 可以知道：一个线程只能创建一个Looper

### <a name="3.Looper构造函数源码分析">3.Looper构造函数源码分析</a>
- 看Looper对象的构造方法

  ```
  final MessageQueue mQueue;
  
  private Looper(boolean quitAllowed) {
      mQueue = new MessageQueue(quitAllowed);
      mThread = Thread.currentThread();
  }
  ```

- 可以看到在其构造方法中初始化了一个MessageQueue对象。MessageQueue也称之为消息队列，特点是先进先出，底层实现是单链表数据结构。
  - 可以得出结论：一个Looper只能拥有一个MessageQueue

### <a name="4.Handler的sendMessage方法是如何运行的">4.Handler的sendMessage方法是如何运行的</a>

1）查看 Handler 的构造函数：

```
public Handler(Callback callback, boolean async) {
    if (FIND_POTENTIAL_LEAKS) {
        final Class<? extends Handler> klass = getClass();
        if ((klass.isAnonymousClass()
        		|| klass.isMemberClass()
        		|| klass.isLocalClass())
        	&& (klass.getModifiers() & Modifier.STATIC) == 0) {
            Log.w(TAG, "The following Handler class should be static or leaks might occur: " +
                klass.getCanonicalName());
        }
    }

    mLooper = Looper.myLooper();
    if (mLooper == null) {
        throw new RuntimeException(
            "Can't create handler inside thread " + Thread.currentThread()
                    + " that has not called Looper.prepare()");
    }
    mQueue = mLooper.mQueue;
    mCallback = callback;
    mAsynchronous = async;
}
```

- 可以知道：
	- Handler 不能是 “内部类，静态类，匿名类”
	- Handler 保存了当前线程的 Looper 对象
	- Handler 保存了 Looper 对象关联的 MessageQueue 对象

2）查看 handler.sendMessage(msg) 源码：

```
private boolean enqueueMessage(MessageQueue queue, Message msg, long uptimeMillis) {
    msg.target = this;
    if (mAsynchronous) {
        msg.setAsynchronous(true);
    }
    return queue.enqueueMessage(msg, uptimeMillis);
}
```

- 可以知道：msg.target 就是 Handler 对象本身

3）查看 MessageQueue 类的 enqueueMessage 方法

```
boolean enqueueMessage(Message msg, long when) {
    ...
    synchronized (this) {
        ...
        msg.markInUse();
        msg.when = when;
        Message p = mMessages;
        boolean needWake;
        if (p == null || when == 0 || when < p.when) {
            msg.next = p;
            mMessages = msg;
            needWake = mBlocked;
        } else {
            needWake = mBlocked && p.target == null && msg.isAsynchronous();
            Message prev;
            for (;;) {
                prev = p;
                p = p.next;
                if (p == null || when < p.when) {
                    break;
                }
                if (needWake && p.isAsynchronous()) {
                    needWake = false;
                }
            }
            msg.next = p;
            prev.next = msg;
        }
        ...
    }
    return true;
}
```

- 可以知道：MessageQueue并没有使用列表将所有的Message保存起来，而是使用Message.next保存下一个Message，从而按照时间将所有的Message排序

### <a name="5.Looper.loop()方法源码分析">5.Looper.loop()方法源码分析</a>

1）查看Looper.loop()方法

```
public static void loop() {
    final Looper me = myLooper();
    if (me == null) {
        throw new RuntimeException("No Looper; Looper.prepare() wasn't called on this thread.");
    }
    final MessageQueue queue = me.mQueue;

	...

    for (;;) {
        Message msg = queue.next(); // might block
        if (msg == null) {
            // No message indicates that the message queue is quitting.
            return;
        }
		
		...	
		try {
		    msg.target.dispatchMessage(msg);
		    dispatchEnd = needEndTime ? SystemClock.uptimeMillis() : 0;
		} finally {
		    if (traceTag != 0) {
		        Trace.traceEnd(traceTag);
		    }
		}
		...
    }
}
```

- 看到Looper.loop()方法里起了一个死循环，不断的判断MessageQueue中的消息是否为空，如果为空则直接return掉。
- MessageQueue的next()方法，大概的实现逻辑就是Message的出栈操作。
- 获取到栈顶的Message对象之后开始执行：msg.target.dispatchMessage(msg)。也就是Handler.dispatchMessage(msg)方法。

2）查看Handler类的dispatchMessage方法

```
public void dispatchMessage(Message msg) {
    if (msg.callback != null) {
        handleCallback(msg);
    } else {
        if (mCallback != null) {
            if (mCallback.handleMessage(msg)) {
                return;
            }
        }
        handleMessage(msg);
    }
}

private static void handleCallback(Message message) {
    message.callback.run();
}
```

- 如果message设置了callback的话，则会调用message.callback.run();
- 如果创建Handler时设置了callback对象的话，则会直接调用callback.handleMessage方法

### <a name="6.Activity的runOnUiThread方法源码分析">6.Activity的runOnUiThread方法源码分析</a>

查看Activity的runOnUiThread方法

```
public final void runOnUiThread(Runnable action) {
    if (Thread.currentThread() != mUiThread) {
        mHandler.post(action);
    } else {
        action.run();
    }
}
```

- 在runOnUiThread首先会判断当前线程是否是UI线程，如果是就直接运行。
- 如果不是则调用UI线程内的Handler的post方法，Handler的post方法，实质也是调用Handler的dispatchMessage方法。由于Handler是属于UI线程的，从而实现了与UI线程的通信。

### <a name="7.view的post方法源码分析">7.view的post方法源码分析</a>

```
public boolean post(Runnable action) {
    final AttachInfo attachInfo = mAttachInfo;
    if (attachInfo != null) {
        return attachInfo.mHandler.post(action);
    }

    // Postpone the runnable until we know on which thread it needs to run.
    // Assume that the runnable will be successfully placed after attach.
    getRunQueue().post(action);
    return true;
}
```

- 可以发现其调用的就是activity中默认保存的handler对象的post方法

### <a name="8.造成ANR的原因">8.造成ANR的原因</a>

- 造成ANR的原因一般有两种：
	- 当前的事件没有机会得到处理（即主线程正在处理前一个事件，没有及时的完成或者looper被某种原因阻塞住了）
	- 当前的事件正在处理，但没有及时完成
- 为了避免ANR异常，Android使用了Handler消息处理机制。让耗时操作在子线程运行。

### <a name="9.总结">9.总结</a>

- 1.主线程中定义Handler对象，ActivityThread的main方法中会自动创建一个looper，并且与其绑定。如果是子线程中直接创建handler对象，则需要手动创建looper。不过手动创建不太友好，需要手动调用quit方法结束looper。这个后面再说
- 2.一个线程中只存在一个Looper对象，只存在一个MessageQueue对象，可以存在N个Handler对象，Handler对象内部关联了本线程中唯一的Looper对象，Looper对象内部关联着唯一的一个MessageQueue对象。
- 3.MessageQueue消息队列不是通过列表保存消息（Message）列表的，而是通过Message对象的next属性关联下一个Message从而实现列表的功能，同时所有的消息都是按时间排序的。

## <a name="第二篇">第二篇</a>

### <a name="1.为什么不允许在子线程中访问UI">1.为什么不允许在子线程中访问UI</a>

- 这是因为Android的UI控件不是线程安全的，如果在多线程中并发访问可能会导致UI控件处于不可预期的状态。
- 那么为什么系统不对UI控件的访问加上锁机制呢？缺点有两个：
	- 首先加上锁机制会让UI访问的逻辑变得复杂。
	- 锁机制会降低UI访问的效率，因为锁机制会阻塞某些线程的执行。
- 所以最简单且高效的方法就是采用单线程模型来处理UI操作。

### <a name="2.避免子线程手动创建Looper">2.避免子线程手动创建Looper</a>

- Toast也是由主线程的Handler处理，因此在子线程不能够弹Toast。如果强制要在自线程弹Toast，需要手动初始化Looper对象。示例代码如下：

	```
	new Thread(new Runnable() {
	    @Override
	    public void run() {
	        Looper.prepare();
	        Toast.makeText(MainActivity.this, "run on Thread", Toast.LENGTH_SHORT).show();
	        Looper.loop();
	    }
	}).start();
	```
- 但是上面的使用方式，是非常危险的一种做法，原因是：
	- 在子线程中，如果手动为其创建Looper，那么在所有的事情完成以后应该调用quit方法来终止消息循环，否则这个子线程就会一直处于等待的状态。
	- 而如果退出Looper以后，这个线程就会立刻终止，因此建议在不需要使用子线程的时候调用 `Looper.myLooper().quit();` 终止Looper。

### <a name="3.Looper死循环会导致应用卡死吗？会消耗大量资源吗？">3.Looper死循环会导致应用卡死吗？会消耗大量资源吗？</a>

- 线程默认没有Looper的，如果需要使用Handler就必须为线程创建Looper。
- 我们经常提到的主线程，也叫UI线程，它就是ActivityThread，ActivityThread被创建时就会初始化Looper，这也是在主线程中默认可以使用Handler的原因。

首先测试Looper死循环是否会导致应用卡死？

```
new Thread(new Runnable() {
	String TAG = "TAG_TEST";
    @Override
    public void run() {
        Looper.prepare();
        Log.e(TAG, "-- run 0 --");
        Toast.makeText(MainActivity.this, "run on Thread", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "-- run 1 --");
        Looper.loop();
        Log.e(TAG, "-- run 2 --");
    }
}).start();

日志打印：
-- run 0 --
-- run 1 --
```

- 得出结论：
	- 也就是说 `Looper.loop();` 里面维护了一个死循环方法。
	- 应用正常运行：说明Looper死循环不会导致应用卡死。

再测试Looper死循环是否会消耗大量资源吗？

- 主线程Looper从消息队列读取消息，当读完所有消息时，主线程阻塞。子线程往消息队列发送消息（往管道文件写数据），主线程即被唤醒。主线程被唤醒只是为了读取消息（从管道文件读取数据），当消息读取完毕，再次睡眠。因此loop的循环并不会消耗CPU性能。
- 在主线程的MessageQueue没有消息时，便阻塞在 `Looper.loop()` 中的 `queue.next()` 中的 `nativePollOnce()` 方法里。此时主线程处于休眠状态，并不会消耗大量CPU资源。
- 原因是这里采用了 Linux 的 pipe/epoll机制：
	- epoll机制，是一种IO多路复用机制，可以同时监控多个描述符，当某个描述符就绪(读或写就绪)，则立刻通知相应程序进行读或写操作，本质类似I/O，即读写是阻塞的。
	- 所以主线程没有消息时，会释放CPU资源进入休眠状态，直到下个消息到达或者有事务发生，才会通过往pipe管道写端写入数据来唤醒主线程工作。

### <a name="4.Handler使用不当造成的内存泄漏分析以及解决方案">4.Handler使用不当造成的内存泄漏分析以及解决方案</a>

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
- Handler内存泄漏总结：
	- 生命周期长的对象引用了生命周期短的对象。匿名内部类的 Handler 持有 Activity 的引用，而发送的 Message 又持有 Handler 的引用，Message 又存在于 MessageQueue 中，而 MessageQueue 又是 Looper 的成员变量，并且 Looper 对象又是存在于静态常量 sThreadLocal 中。
	- 从上面的分析中，可以知道，想要防止 Handler 内存泄漏，一种方法是把 sThreadLocal  到 Activity 的引用链断开就行了。

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

### <a name="5.ActivityThread,Looper,MessageQueue,Message,Handler大总结">5.ActivityThread,Looper,MessageQueue,Message,Handler大总结</a>

#### 1）ActivityThread简介

- 进程：每个app运行时前首先创建一个进程，该进程是由Zygote fork出来的，大多数情况一个App就运行在一个进程中，除非在AndroidManifest.xml中配置Android:process属性，或通过native代码fork进程。
- 线程：new Thread().start都会创建一个新的线程。该线程与App所在进程之间资源共享，从Linux角度来说进程与线程除了是否共享资源外，并没有本质的区别，都是一个task_struct结构体，在CPU看来进程或线程无非就是一段可执行的代码，CPU采用CFS调度算法，保证每个task都尽可能公平的享有CPU时间片。
- ActivityThread的主线程就是由Zygote fork而创建。
- ActivityThread创建时会执行main方法，该方法会创建Looper，并开始Looper.loop()阻塞式轮询。
- 由于ActivityThread的上下文是ContextWrapper，所以可以通过ContextWrapper的getMainLooper方法获取得到ActivityThread中的Looper对象。

#### 2）Looper简介

- Looper.prepare()
	- Looper.prepare()会创建Looper对象。
	- Looper创建时会自动创建一个MessageQueue对象。
	- 一个线程只能创建一个Looper对象，因此Looper.prepare()不能多次调用。
	- 创建完成后的Looper会被放到ThreadLocal中保存，此时Looper会被当作线程局部变量保存。
- Looper.loop()
	- 当调用Looper.loop()时Looper开始阻塞式循环消息队列。
	- Looper.loop()内部是一个死循环，通过queue.next()取出消息。
	- Looper.loop()内部通过msg.target.dispatchMessage(msg)处理消息。
	- 当没有消息时，便会阻塞在 Looper.loop() 中的 queue.next() 中的 nativePollOnce() 方法里。原因是这里采用了 Linux 的 pipe/epoll机制。当读或写就绪时，则立刻通知相应程序进行读或写操作，否则线程阻塞进入休眠状态。类似于I/O读写是阻塞的。
	- Looper.loop()阻塞时，主线程处于休眠状态，并不会消耗大量CPU资源。
- 当Looper不使用时需调用quit()方法关闭消息轮询。但是一旦调用quit()方法，Looper所在的线程也会被关闭。
- 可以通过 `Looper.getMainLooper()` 获取主线程的Looper对象。

#### 3）MessageQueue简介

- 消息队列随着Looper的创建而创建，特点是先进先出，底层实现是单链表数据结构。
- enqueueMessage()方法用于消息入队，通过Message对象的next属性关联下一个Message从而实现链表的功能，同时所有的消息都是按时间排序的。
- next()方法是获取下一个消息的方法，无消息时该方法阻塞，原因是这里采用了 Linux 的 pipe/epoll机制。当读或写就绪时，则立刻通知相应程序进行读或写操作，否则线程阻塞进入休眠状态。类似于I/O读写是阻塞的。

#### 4）Message简介

- 消息的标识符是what，同时可以携带obj数据
- 消息中存有Handler对象，也就是target字段
- 消息通过Message.obtain()方法创建
	- Message obtain()
	- Message obtain(Handler h, Runnable callback)

#### 5）Handler简介

- Handler创建时需要关联一个Looper。
- 如果是在主线程中，那么可以直接new Handler()来创建一个Handler。
- 如果在自线程中创建Handler，需要按照以下流程

	```
	Looper.prepare();
	Handler handler = new Handler();
	Looper.loop();
	```
	
- 如果一段代码希望运行在主线程中，可以有以下几种方式
	- handler.post(Runnable r)
	- handler.postDelayed(Runnable r, long delayMillis)
	- view.post(Runnable r)
	- new Handler(Looper.getMainLooper()).post(Runnable r)
- Handler发消息，可以有以下几种方式
	- boolean sendMessage(Message msg)
	- boolean sendEmptyMessage(int what)
	- boolean sendEmptyMessageAtTime(int what, long uptimeMillis)
	- boolean sendMessageDelayed(Message msg, long delayMillis)
