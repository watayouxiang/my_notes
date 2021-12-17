# Handler消息通讯详解

> Handler源码整体框架
>
> Handler实现线程间通信的方案思想
>
> Handler源码面试问题解析

## Handler面试题

- Handler原理是什么，简单说下
  - 核心是解决线程切换。
  - Looper.prepare将Looper与Thread绑定，Looper.loop()开始死循环读取消息。
    - Looper中持有MessageQueue
    - Looper中持有ThreadLocal对象，用于实现一个线程只能有一个Looper对象，Thread中有ThreadLocalMap，当调用ThreadLocal.set方法时，将ThreadLocal作为key，Looper作为value存入Thread中的ThreadLocalMap。在当前Thread调用ThreadLocal.get方法时，就能拿到Looper对象
    - Looper调用Looper.loop()时，处理消息时通过msg.target.handleMessage(msg)方式
  - MessageQueue利用生产者消费者模式，实现批量存消息存储，一个个消息读取
  - Message持有Handler对象
  - Handler持有Looper对象，还有存取消息的方法enqueueMessage(msg), handleMessage(msg)
- 为什么不能在子线程更新UI？
- 一个线程有几个looper? 如何保证，又可以有几个Handler
  - 一个线程只有一个Looper，通过ThreadLocal来保证，可以有任意多个Handler
- 一个线程是怎么确保只有一个Looper的？
  - 通过ThreadLocal来保证，Thread中有ThreadLocalMap，当调用ThreadLocal.set方法时，将ThreadLocal作为key，Looper作为value存入Thread中的ThreadLocalMap。在当前Thread调用ThreadLocal.get方法时，就能拿到Looper对象。
- handler内存泄漏的原因，其他内部类为什么没有这个问题
  - handler内存泄漏的原因是短生命周期的对象持有长生命周期的对象造成的，并不是内部类持有外部类引用导致。
  - Thread -- Looper -- MessageQueue -- Message -- Handler -- Activity
- 为什么主线程可以new Handler  其他子线程可以吗  怎么做
- Message可以如何创建？哪种效果更好，为什么？
- Handler中的生产者-消费者设计模式你理解不？
- 子线程中维护Looper在消息队列无消息的时候处理方案是怎么样的
- 既然存在多个Handler往MessageQueue中添加数据（发消息时各个Handler处于不同线程），内部如何保证安全
- 我们使用Message是应该如何创建它
- Looper死循环为什么不会导致线程卡死
- 关于ThreadLocal，谈谈你的理解？
  - Looper中持有ThreadLocal对象，用于实现一个线程只能有一个Looper对象，Thread中有ThreadLocalMap，当调用ThreadLocal.set方法时，将ThreadLocal作为key，Looper作为value存入Thread中的ThreadLocalMap。在当前Thread调用ThreadLocal.get方法时，就能拿到Looper对象
- 使用Hanlder的postDealy()后消息队列会发生什么变化？
- 为什么不能在子线程更新UI？

## Android分层

- 应用层
- Framework
  - App内部通讯：Handler
  - App间通讯：Binder
- Android内核、虚拟机（art和dalvik）
- 驱动
  - binder
- Linux内核

## 手写Handler(体验架构演进)

> Handler核心是解决线程切换的问题，而Handler中的Message机制，顺带解决了线程通信的问题。
>
> 线程通信，其实就是数据共享。而申明全局变量也能实现线程通信，因此这不是关键。
>
> Handler机制就相当一台跑步机：
>
> - Handler：提供一个 “放货物” 和 “取货物” 的方法
>   - Handler.enqueueMessage() 放货物
>   - Handler.handleMessage() 取货物
> - Message：货物
> - MessageQueue：传送带
> - Looper：发电机

### 1）实现收发消息

> 1. 大量消息过来，容易阻塞 OOM
> 2. 不能做到线程通信 + 线程切换

```java
package com.watayouxiang.handlerdemo;

public class Handler {
    public void enqueueMessage(Message msg) {
        handleMessage(msg);
    }

    public void handleMessage(Message msg) {
    }
}

// ------------------------------------------------

package com.watayouxiang.handlerdemo;

public class Message {
    Object obj;

    public Message() {
    }

    public Message(String obj) {
        this.obj = obj;
    }
}

// ------------------------------------------------

package com.watayouxiang.handlerdemo;

public class ActivityThread {
    public static void main(String[] args) throws Exception {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Object obj = msg.obj;
            }
        };
        new Thread() {
            @Override
            public void run() {
                super.run();
                handler.enqueueMessage(new Message("hello"));
            }
        }.start();
    }
}
```

### 2）实现线程通信、能处理大量消息

> 阻塞队列：生产者消费者模式，将消息的存和取分开处理，避免大量消息过来时阻塞
>
> 死循环：开启死循环不断读取消息，读到的消息此时运行在开启死循环的线程中，从而实现线程通信

```java
package com.watayouxiang.handlerdemo;

public class Message {
    Object obj;

    public Message() {
    }

    public Message(String obj) {
        this.obj = obj;
    }
}

// --------------------------------------------------------------

package com.watayouxiang.handlerdemo;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MessageQueue {
    BlockingQueue<Message> queue = new ArrayBlockingQueue<Message>(100);
    public void enqueueMessage(Message msg){
        try {
            queue.put(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Message next(){
        Message msg = null;
        try {
            msg = queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return msg;
    }
}

// --------------------------------------------------------------

package com.watayouxiang.handlerdemo;

public class Handler {
    MessageQueue messageQueue = new MessageQueue();

    public void looper() {
        for (; ; ) {
            Message msg = messageQueue.next();
            handleMessage(msg);
        }
    }

    public void enqueueMessage(Message msg) {
        messageQueue.enqueueMessage(msg);
    }

    public void handleMessage(Message msg) {
    }
}

// --------------------------------------------------------------

package com.watayouxiang.handlerdemo;

public class ActivityThread {
    // Thread-0 send nihao
    // main receive nihao
    public static void main(String[] args) throws Exception {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                System.out.println(Thread.currentThread().getName() + " receive " + msg.obj.toString());
            }
        };

        new Thread() {
            @Override
            public void run() {
                Message msg = new Message("nihao");
                System.out.println(Thread.currentThread().getName() + " send " + msg.obj.toString());
                handler.enqueueMessage(msg);
                super.run();
            }
        }.start();

        handler.looper();
    }
}
```

### 3）一个线程只能有一个消息队列

> 1. 怎么让一个线程只有一个消息队列
>
> 2. ThreadLocal的使用和解析
>
>    得出结论：Thread -- Looper -- MessageQueue -- Message -- Handler -- Activity
>
>    后续问题：主线程调用Looper.loop() 为什么不会死循环？

```java
package com.watayouxiang.handlerdemo;

public class Handler {
    Looper mLooper;

    public Handler() {
        this.mLooper = Looper.myLooper();
    }

    public void enqueueMessage(Message msg) {
        msg.target = this;
        mLooper.messageQueue.enqueueMessage(msg);
    }

    public void handleMessage(Message msg) {
    }
}

// --------------------------------------------------------------

package com.watayouxiang.handlerdemo;

public class Looper {
    MessageQueue messageQueue;
    private static final ThreadLocal<Looper> sThreadLocal = new ThreadLocal<Looper>();

    private Looper() {
        messageQueue = new MessageQueue();
    }

    /**
     * 获取与该线程绑定的Looper
     */
    public static Looper myLooper(){
        return sThreadLocal.get();
    }

    /**
     * 将线程和Looper绑定
     * <p>
     * 为了确保一个线程只有一个消息队列：
     * 因为一个 Looper 对应一个消息队列，所以只要 Looper 唯一，那么消息队列也就是唯一，同时线程也是唯一。
     * 通过查看 Thread 源码可以知道，一个 Thread 对应一个 ThreadLocal.ThreadLocalMap。
     * ThreadLocal.ThreadLocalMap 的 key = ThreadLocal<Looper>，value = Looper。
     * 所以如果要取出 Looper，只要持有 ThreadLocal.ThreadLocalMap 的 key（ThreadLocal）就可以了。
     */
    public static void prepare() {
        if (sThreadLocal.get() != null) {
            throw new RuntimeException("Only one Looper may be created per thread");
        }
        sThreadLocal.set(new Looper());
    }

    /**
     * 开启死循环
     * <p>
     * 这里要调用 handleMessage() 方法，必须要要拿到 handler 对象。
     * 而在一个线程中 handler 是可以无数多个的，是无法管理的。
     * 所以只能妥协让 message 持有 handler 对象。
     */
    public static void looper() {
        Looper looper = sThreadLocal.get();
        MessageQueue messageQueue = looper.messageQueue;
        for (; ; ) {
            Message msg = messageQueue.next();
            msg.target.handleMessage(msg);
        }
    }
}

// --------------------------------------------------------------

package com.watayouxiang.handlerdemo;

public class Message {
    Handler target;
    Object obj;

    public Message() {
    }

    public Message(String obj) {
        this.obj = obj;
    }
}

// --------------------------------------------------------------

package com.watayouxiang.handlerdemo;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MessageQueue {
    BlockingQueue<Message> queue = new ArrayBlockingQueue<Message>(100);
  
    public void enqueueMessage(Message msg){
        try {
            queue.put(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Message next(){
        Message msg = null;
        try {
            msg = queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return msg;
    }
}

// --------------------------------------------------------------

package com.watayouxiang.handlerdemo;

public class ActivityThread {
    public static void main(String[] args) throws Exception {
        Looper.prepare();

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                System.out.println(Thread.currentThread().getName() + " receive " + msg.obj.toString());
            }
        };

        new Thread() {
            @Override
            public void run() {
                super.run();
                Message msg = new Message("nihao");
                System.out.println(Thread.currentThread().getName() + " send " + msg.obj.toString());
                handler.enqueueMessage(msg);
            }
        }.start();

        Looper.looper();
    }
}
```



