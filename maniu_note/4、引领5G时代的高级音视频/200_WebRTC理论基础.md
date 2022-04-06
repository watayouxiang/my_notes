
# WebRTC理论基础

## WebRTC相关名词

> WebRTC 协议介绍：[https://developer.mozilla.org/zh-CN/docs/Web/API/WebRTC_API/Protocols](https://links.jianshu.com/go?to=https%3A%2F%2Fdeveloper.mozilla.org%2Fzh-CN%2Fdocs%2FWeb%2FAPI%2FWebRTC_API%2FProtocols)

### ICE

-   交互式连接设施 [Interactive Connectivity Establishment (ICE)](https://links.jianshu.com/go?to=http%3A%2F%2Fen.wikipedia.org%2Fwiki%2FInteractive_Connectivity_Establishment)
-   允许你的浏览器和对端浏览器建立连接的协议框架
-   在实际的网络当中，有很多原因能导致简单的从A端到B端直连不能如愿完成。这需要绕过阻止建立连接的防火墙，给你的设备分配一个唯一可见的地址（通常情况下我们的大部分设备没有一个固定的公网地址），如果路由器不允许主机直连，还得通过一台服务器转发数据。ICE通过使用以下几种技术完成上述工作。


### NAT

-   网络地址转换协议 [Network Address Translation (NAT)](https://links.jianshu.com/go?to=http%3A%2F%2Fen.wikipedia.org%2Fwiki%2FNAT)
-   用来给你的（私网）设备映射一个公网的IP地址的协议。

### STUN

-   NAT的会话穿越协议 [Session Traversal Utilities for NAT (STUN)](https://links.jianshu.com/go?to=http%3A%2F%2Fen.wikipedia.org%2Fwiki%2FSTUN)
-   NAT的会话穿越功能，是一个允许位于NAT后的客户端找出自己的公网地址，判断出路由器阻止直连的限制方法的协议。

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/00b3c171b77941228c47a55646f680b0~tplv-k3u1fbpfcp-zoom-1.image)

STUN.png

### TURN

-   NAT的中继穿越协议 [Session Traversal Utilities for NAT (STUN)](https://links.jianshu.com/go?to=http%3A%2F%2Fen.wikipedia.org%2Fwiki%2FSTUN)
-   NAT的中继穿越方式，通过TURN服务器中继所有数据的方式来绕过“对称型NAT”。你需要在TURN服务器上创建一个连接，然后告诉所有对端设备发包到服务器上，TURN服务器再把包转发给你。很显然这种方式是开销很大的，所以只有在没得选择的情况下采用。

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/20b7b819533f48cfa4e1f827037045b8~tplv-k3u1fbpfcp-zoom-1.image)

TURN.png

### SDP

-   会话描述协议 [Session Description Protocol (SDP)](https://links.jianshu.com/go?to=http%3A%2F%2Fen.wikipedia.org%2Fwiki%2FSession_Description_Protocol)
-   是一个描述多媒体连接内容的协议，例如分辨率，格式，编码，加密算法等。所以在数据传输时两端都能够理解彼此的数据。本质上，这些描述内容的元数据并不是媒体流本身。

## WebRTC流程图

> WebRTC_API：[https://developer.mozilla.org/zh-CN/docs/Web/API/WebRTC_API](https://links.jianshu.com/go?to=https%3A%2F%2Fdeveloper.mozilla.org%2Fzh-CN%2Fdocs%2FWeb%2FAPI%2FWebRTC_API)

### WebRTC Signaling Diagram

[https://media.prod.mdn.mozit.cloud/attachments/2016/01/27/12363/9d667775214ae0422fae606050f60c1e/WebRTC%20-%20Signaling%20Diagram.svg](https://links.jianshu.com/go?to=https%3A%2F%2Fmedia.prod.mdn.mozit.cloud%2Fattachments%2F2016%2F01%2F27%2F12363%2F9d667775214ae0422fae606050f60c1e%2FWebRTC%2520-%2520Signaling%2520Diagram.svg)

### WebRTC ICE Candidate Exchange

[https://media.prod.mdn.mozit.cloud/attachments/2016/01/27/12365/b5bcd9ecac08ae0bc89b6a3e08cfe93c/WebRTC%20-%20ICE%20Candidate%20Exchange.svg](https://links.jianshu.com/go?to=https%3A%2F%2Fmedia.prod.mdn.mozit.cloud%2Fattachments%2F2016%2F01%2F27%2F12365%2Fb5bcd9ecac08ae0bc89b6a3e08cfe93c%2FWebRTC%2520-%2520ICE%2520Candidate%2520Exchange.svg)

### The entire exchange

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/9509834487a44d5b8671e29ed8401811~tplv-k3u1fbpfcp-zoom-1.image)
