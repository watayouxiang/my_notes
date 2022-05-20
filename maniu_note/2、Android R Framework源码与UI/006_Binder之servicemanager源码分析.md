# servicemanager源码分析

> 源码在线查阅网站：
>
> http://androidxref.com/9.0.0_r3/
>
> http://aospxref.com/android-9.0.0_r61/xref/frameworks/

## ServiceManager源码位置

<img src="005_Binder之servicemanager源码分析.assets/image-20220324200536043.png" alt="image-20220324200536043" style="zoom:50%;" />

## servicemanager源码分析

> ServiceManager 用于管理 Binder IPC（服务注册、服务发现、服务调用）

### Binder IPC 架构图

<img src="004_Binder之手写IPC进程通信.assets/image-20220322212333960.png" alt="image-20220322212333960" style="zoom:50%;" />

### Binder4层源码链接

[Binder4层源码链接](002_Binder之linux内存基础.assets/Binder4层源码)

### binder源码时序图

<img src="005_Binder之servicemanager源码分析.assets/image-20220324202623858.png" alt="image-20220324202623858" style="zoom:100%;" />

### 源码分析Proxy

只要搞清楚 Stub的内部类Proxy的mRemote 是什么，就能了解Binder内部调用过程。

- mRemote 等价于 BinderProxy
  - BinderProxy 不是服务端Binder对象，只是服务端Binder对象的代理
  - BinderProxy 内部持有 BpBinder
    - BpBinder是C++里面构建的java对象，负责发送消息
    - BpBinder是在ProcessState中创建的

由上面分析可以得出：Proxy是消息发送者，Proxy内部持有BinderProxy，而BinderProxy内部持有BpBinder，真正发送消息的是BpBinder对象。

### 源码分析Stub

ServiceManagerNative.java 的内部类 ServiceManagerProxy 就是Stub

### Binder驱动的职责

- 维护进程队列
- 查找进程信息

### ServiceManager职责

Binder驱动相当于ServiceManager的一个工具，ServiceManager的作用是维护进程间通信（注册服务、发现服务、调用服务）。
