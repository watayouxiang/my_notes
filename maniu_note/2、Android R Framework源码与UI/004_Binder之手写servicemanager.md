# 手写ipc进程通信

> 在Binder源码中，实现跨进程通信（也就是实现IPC通信）不是难点。
>
> 真正的难点是Binder的架构，也就是service_manager的实现。
>
> service_manager主要功能有三个：
>
> - 服务注册
> - 服务发现
> - 服务调用

## service_manager

<img src="004_Binder之手写servicemanager.assets/image-20220322212333960.png" alt="image-20220322212333960" style="zoom:50%;" />

- 发消息：BpBinder / Proxy

- 收消息：BBinder / Stub