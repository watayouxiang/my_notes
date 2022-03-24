# 手写ServiceManager

> 在Binder源码中，实现跨进程通信（也就是实现IPC通信）不是难点。
>
> 真正的难点是Binder的架构，也就是ServiceManager的实现。
>
> ServiceManager主要功能有三个：
>
> - 服务注册
> - 服务发现
> - 服务调用

## ServiceManager架构图

<img src="004_Binder之手写servicemanager.assets/image-20220322212333960.png" alt="image-20220322212333960" style="zoom:50%;" />

- 发消息：BpBinder / Proxy
- 收消息：BBinder / Stub

## 手写ServiceManager

### 使用效果展示

- 现有两个进程：`com.watayouxiang.demo.binder` 和 `com.watayouxiang.demo.binder2`
- 此时进程 `com.watayouxiang.demo.binder2`  要调用进程  `com.watayouxiang.demo.binder` 的服务
- 



