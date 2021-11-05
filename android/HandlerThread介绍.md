# HandlerThread介绍

目录

- [1.HandlerThread介绍](#1.HandlerThread介绍)
- [2.HandlerThread的基本用法](#2.HandlerThread的基本用法)
- [3.HandlerThread源码分析](#3.HandlerThread源码分析)

---

### <a name="1.HandlerThread介绍">1.HandlerThread介绍</a>

> Handy class for starting a new thread that has a looper. 
> 
> The looper can then be used to create handler classes. 
> 
> Note that start() must still be called.

- `public class HandlerThread extends Thread` 可以知道HandlerThread本质上就是一个线程
- run()方法中调用了Looper.prepare()和Looper.loop()，所以可知该线程自带Handler对象。
- 什么时候会用到：
	- 当APP中创建多个线程，为了让多个线程之间能够方便的通信，会使用Handler实现线程间的通信。

### <a name="2.HandlerThread的基本用法">2.HandlerThread的基本用法</a>

```
@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_handler_thread);

    //定义 handlerThread 线程
    HandlerThread handlerThread = new HandlerThread("handlerThread-1") {
        @Override
        public void run() {
            Log.d(TAG, Thread.currentThread().getName() + ": run start");
            super.run();
            Log.d(TAG, Thread.currentThread().getName() + ": run end");
        }
    };
    //开启 handlerThread
    handlerThread.start();
    //只有开启线程后调 handlerThread.getLooper() 才有值
    Handler handler = new Handler(handlerThread.getLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, Thread.currentThread().getName() + ": handleMessage");
        }
    };
    //向 handlerThread 发消息
    handler.sendEmptyMessage(1001);
    //销毁 handlerThread
    handlerThread.quit();
}
```

### <a name="3.HandlerThread源码分析">3.HandlerThread源码分析</a>

```
@Override
public void run() {
    mTid = Process.myTid();
    Looper.prepare();
    synchronized (this) {
        mLooper = Looper.myLooper();
        notifyAll();
    }
    Process.setThreadPriority(mPriority);
    onLooperPrepared();
    Looper.loop();
    mTid = -1;
}
```

- 线程运行后HandlerThread线程中自动创建了该线程的Looper与MessageQueue。
- 在调用Looper.loop()方法之前调用了一个空的实现方法onLooperPrepared()，所以可以实现onLooperPrepared()方法，对Looper做一些的初始化操作。
- 只有调用了mHandlerThread.start()方法后，mHandlerThread.getLooper()才有值。

```
public boolean quit() {
    Looper looper = getLooper();
    if (looper != null) {
        looper.quit();
        return true;
    }
    return false;
}
```

- 当调用mHandlerThread.quit()后，会关闭MessageQueue，并移除所有的Message。
