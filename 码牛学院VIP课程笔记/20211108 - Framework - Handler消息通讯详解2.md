# Handler消息通讯详解2

> Looper消息泵机制
>
> Looper死循环是在UI线程，那为什么不会阻塞UI线程呢
>
> Linux pipe/epoll 机制
>
> pipe管道与CPU休眠状态
>
> Message回收池设计，单向链表详解
>
> Message的回收与复用机制
>
> 手写Handler

## 1、Message消息回收池

Message源码：

```java
private static Message sPool;// 消息回收池（只需要记录第一条消息地址即可）
private static int sPoolSize = 0;// 消息回收池长度
private static final int MAX_POOL_SIZE = 50;// 消息回收池最大容量

// 获取 “消息回收池” 的消息（消息的复用机制）
public static Message obtain() {
    synchronized (sPoolSync) {
        if (sPool != null) {
            Message m = sPool;
            sPool = m.next;
            m.next = null;
            m.flags = 0; // clear in-use flag
            sPoolSize--;
            return m;
        }
    }
    return new Message();
}

/**
 * 注释可以看出：调用Handler移除消息的方法时，那些被移除的消息就会添加进“消息回收池”中，
 *  						比如 释放的触摸事件、取消的错误事件 时。
 * <p> 
 * Return a Message instance to the global pool.
 * <p>
 * You MUST NOT touch the Message after calling this function because it has
 * effectively been freed.  It is an error to recycle a message that is currently
 * enqueued or that is in the process of being delivered to a Handler.
 * </p>
 */
public void recycle() {
    if (isInUse()) {
        if (gCheckRecycle) {
            throw new IllegalStateException("This message cannot be recycled because it "
                    + "is still in use.");
        }
        return;
    }
    recycleUnchecked();
}

// 往 “消息回收池” 中添加消息（消息的复用机制）
// 当使用Handler删除某条消息的时候，会分别调用MessageQueue的如下三个方法：
// MessageQueue 的 removeMessages(Handler h, int what, Object object)
// MessageQueue 的 removeMessages(Handler h, Runnable r, Object object)
// MessageQueue 的 removeCallbacksAndMessages(Handler h, Object object)
// 从而实现往 “消息回收池” 中添加消息
void recycleUnchecked() {
    // Mark the message as in use while it remains in the recycled object pool.
    // Clear out all other details.
    flags = FLAG_IN_USE;
    what = 0;
    arg1 = 0;
    arg2 = 0;
    obj = null;
    replyTo = null;
    sendingUid = -1;
    when = 0;
    target = null;
    callback = null;
    data = null;

    synchronized (sPoolSync) {
        if (sPoolSize < MAX_POOL_SIZE) {
            next = sPool;
            sPool = this;
            sPoolSize++;
        }
    }
}

// 这是 MessageQueue 的 removeMessages(Handler h, int what, Object object)源码
void removeMessages(Handler h, Runnable r, Object object) {
    if (h == null || r == null) {
        return;
    }

    synchronized (this) {
        Message p = mMessages;

        // Remove all messages at front.
        while (p != null && p.target == h && p.callback == r
               && (object == null || p.obj == object)) {
            Message n = p.next;
            mMessages = n;
            p.recycleUnchecked();
            p = n;
        }

        // Remove all messages after front.
        while (p != null) {
            Message n = p.next;
            if (n != null) {
                if (n.target == h && n.callback == r
                    && (object == null || n.obj == object)) {
                    Message nn = n.next;
                    n.recycleUnchecked();
                    p.next = nn;
                    continue;
                }
            }
            p = n;
        }
    }
}
```

- 消息回收池是 “单向链表”
- Message.obtain()方式是复用消息回收池的消息
- 不推荐 new Message() 方式创建消息，推荐 Message.obtain() 方式获取消息回收池中的消息
- 当使用Handler删除某条消息的时候，会分别调用MessageQueue的如下三个方法。从而实现往消息回收池中添加消息
  - removeMessages(Handler h, int what, Object object)
  - removeCallbacksAndMessages(Handler h, Object object)
  - removeMessages(Handler h, Runnable r, Object object)
- 从源码中可知，被释放的触摸事件、被取消的错误事件 都会尝试进入“消息回收池”

## 2、Handler定时发送消息

> ArrayBlockingQueue 可以实现消息按顺序发送，但不能实现定时发送消息
>
> ArrayBlockingQueue 是以锁的方式实现阻塞与唤醒，不满足开发需求

Handler源码：

```java
// 延迟发送
public final boolean sendMessageDelayed(Message msg, long delayMillis) {
    if (delayMillis < 0) {
        delayMillis = 0;
    }
    return sendMessageAtTime(msg, SystemClock.uptimeMillis() + delayMillis);
}

// 指定时间发送消息
public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
    MessageQueue queue = mQueue;
    if (queue == null) {
        RuntimeException e = new RuntimeException(
                this + " sendMessageAtTime() called with no mQueue");
        Log.w("Looper", e.getMessage(), e);
        return false;
    }
    return enqueueMessage(queue, msg, uptimeMillis);
}
```

MessageQueue源码：

```java
// 这个就是消息链表（单向链表）
// 持有消息链表的第一条消息，就等于持有了整个消息链表
Message mMessages;

// “消息” 按时间顺序插入 “消息队列”
boolean enqueueMessage(Message msg, long when) {
    synchronized (this) {
        msg.markInUse();
        msg.when = when;
        Message p = mMessages;
        boolean needWake;
        if (p == null || when == 0 || when < p.when) {
            // 如果头消息为空，说明此时是第一条消息
            // New head, wake up the event queue if blocked.
            msg.next = p;
            mMessages = msg;
            needWake = mBlocked;
        } else {
            // Inserted within the middle of the queue.  Usually we don't have to wake
            // up the event queue unless there is a barrier at the head of the queue
            // and the message is the earliest asynchronous message in the queue.
            needWake = mBlocked && p.target == null && msg.isAsynchronous();
            // 遍历消息链表，寻找插入位置
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
            // 消息插入消息链表
            msg.next = p; // invariant: p == prev.next
            prev.next = msg;
        }

        // We can assume mPtr != 0 because mQuitting is false.
        if (needWake) {
            nativeWake(mPtr);
        }
    }
    return true;
}
```

## 3、epoll机制

> Looper死循环是在UI线程，那为什么不会阻塞UI线程？

MessageQueue源码：

```java
Message next() {
    // Return here if the message loop has already quit and been disposed.
    // This can happen if the application tries to restart a looper after quit
    // which is not supported.
    final long ptr = mPtr;
    if (ptr == 0) {
        return null;
    }

    int pendingIdleHandlerCount = -1; // -1 only during first iteration
    int nextPollTimeoutMillis = 0;
    for (;;) {
        if (nextPollTimeoutMillis != 0) {
            Binder.flushPendingCommands();
        }

        nativePollOnce(ptr, nextPollTimeoutMillis);// 这里实现阻塞式功能（c语言的epoll机制）

        synchronized (this) {
            // Try to retrieve the next message.  Return if found.
            final long now = SystemClock.uptimeMillis();
            Message prevMsg = null;
            Message msg = mMessages;
            if (msg != null && msg.target == null) {
                // Stalled by a barrier.  Find the next asynchronous message in the queue.
                do {
                    prevMsg = msg;
                    msg = msg.next;
                } while (msg != null && !msg.isAsynchronous());
            }
            if (msg != null) {
                if (now < msg.when) {
                    // Next message is not ready.  Set a timeout to wake up when it is ready.
                    nextPollTimeoutMillis = (int) Math.min(msg.when - now, Integer.MAX_VALUE);
                } else {
                    // Got a message.
                    mBlocked = false;
                    if (prevMsg != null) {
                        prevMsg.next = msg.next;
                    } else {
                        mMessages = msg.next;
                    }
                    msg.next = null;
                    if (DEBUG) Log.v(TAG, "Returning message: " + msg);
                    msg.markInUse();
                    return msg;
                }
            } else {
                // No more messages.
                nextPollTimeoutMillis = -1;
            }

            // Process the quit message now that all pending messages have been handled.
            if (mQuitting) {
                dispose();
                return null;
            }

            // If first time idle, then get the number of idlers to run.
            // Idle handles only run if the queue is empty or if the first message
            // in the queue (possibly a barrier) is due to be handled in the future.
            if (pendingIdleHandlerCount < 0
                    && (mMessages == null || now < mMessages.when)) {
                pendingIdleHandlerCount = mIdleHandlers.size();
            }
            if (pendingIdleHandlerCount <= 0) {
                // No idle handlers to run.  Loop and wait some more.
                mBlocked = true;
                continue;
            }

            if (mPendingIdleHandlers == null) {
                mPendingIdleHandlers = new IdleHandler[Math.max(pendingIdleHandlerCount, 4)];
            }
            mPendingIdleHandlers = mIdleHandlers.toArray(mPendingIdleHandlers);
        }

        // Run the idle handlers.
        // We only ever reach this code block during the first iteration.
        for (int i = 0; i < pendingIdleHandlerCount; i++) {
            final IdleHandler idler = mPendingIdleHandlers[i];
            mPendingIdleHandlers[i] = null; // release the reference to the handler

            boolean keep = false;
            try {
                keep = idler.queueIdle();
            } catch (Throwable t) {
                Log.wtf(TAG, "IdleHandler threw exception", t);
            }

            if (!keep) {
                synchronized (this) {
                    mIdleHandlers.remove(idler);
                }
            }
        }

        // Reset the idle handler count to 0 so we do not run them again.
        pendingIdleHandlerCount = 0;

        // While calling an idle handler, a new message could have been delivered
        // so go back and look again for a pending message without waiting.
        nextPollTimeoutMillis = 0;
    }
}
```

- MessageQueue 的 next() 方法中的 nativePollOnce(ptr, nextPollTimeoutMillis); 实现了阻塞式功能。内部是利用了c语言的epoll机制实现的。
- epoll机制详情，请自行查阅！！！

