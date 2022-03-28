
# WebRTC音视频会议实现思路

1.  连接服务器，并通过服务器打通两个客户端的网络通道。
2.  从摄像头和麦克风获取媒体流。
3.  将本地媒体流通过网络通道传送给对方的客户端。
4.  渲染播放接收到的媒体流。

## 创建PeerConnectionFactory
PeerConnectionFactory在webrtc中很重要， Factory是工厂，工厂可以生产很多很多的PeerConnection 摄像头、麦克风这些设备只能是进程独占方式的，所有只有一个。

首先需要初始化PeerConnectionFactory，这是WebRTC的核心工具类，初始化方法如下：

```
PeerConnectionFactory.initializeAndroidGlobals(
    context,//上下文，可自定义监听
    initializeAudio,//是否初始化音频，布尔值
    initializeVideo,//是否初始化视频，布尔值
    videoCodecHwAcceleration,//是否支持硬件加速，布尔值
    renderEGLContext);//是否支持硬件渲染，布尔值
```

然后就可以获得对象：

```
PeerConnectionFactory factory= new PeerConnectionFactory();
```

## 获取媒体流

### 第一步：获取视频源videoSource

```
String frontCameraName = VideoCapturerAndroid.getNameOfFrontFacingDevice();
VideoCapturer videoCapturer = VideoCapturerAndroid.create(frontCameraName);
VideoSource videoSource = factory.createVideoSource(videoCapturer,videoConstraints);
```

其中videoConstraints是对视频流的一些限制，按如下方法创建。

```
MediaConstraints videoConstraints = new MediaConstraints();
videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxHeight", Integer.toString(pcParams.videoHeight)));
videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxWidth", Integer.toString(pcParams.videoWidth)));
videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxFrameRate", Integer.toString(pcParams.videoFps)));
videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minFrameRate", Integer.toString(pcParams.videoFps)));
```

### 第二步：获取音频源audioSource

音频源的获取简单许多：

```
AudioSource audioSource = factory.createAudioSource(new MediaConstraints());
```

### 第三步：获得封装VideoTrack/AudioTrack

```
VideoTrack/AudioTrack 是 VideoSource/AudioSource 的封装，方便他们的播放和传输：
​
VideoTrack videoTrack = factory.createVideoTrack("ARDAMSv0", videoSource);
AudioTrack audioTrack = factory.createAudioTrack("ARDAMSa0", audioSource);
```

### 第四步：获取媒体流localMS

其实 VideoTrack/AudioTrack 已经可以播放了，不过我们先不考虑本地播放。那么如果要把他们发送到对方客户端，我们需要把他们添加到媒体流中：

```
MediaStream localMS=factory.createLocalMediaStream("ARDAMS");
localMS.addTrack(videoTrack);
localMS.addTrack(audeoTrack);
```

然后，如果有建立好的连接通道，我们就可以把 localMS 发送出去了。

## 建立PeerConnection

WebRTC是基于P2P的，但是在连接通道建立好之前，我们仍然需要服务器帮助传递信令，而且需要服务器帮助进行网络穿透。大体需要如下几个步骤。

### 第一步：创建PeerConnection的对象。

```
PeerConnection pc = factory.createPeerConnection(
    iceServers,//ICE服务器列表
    pcConstraints,//MediaConstraints
    context);//上下文，可做监听
```

iceServers 我们下面再说。 pcConstraints是媒体限制，可以添加如下约束：

```
pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
pcConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
​
```

监听器建议同时实现SdpObserver、PeerConnection.Observer两个接口。

### 第二步：信令交换

1.  建立连接通道时我们需要在WebRTC两个客户端之间进行一些信令交换，我们以A作为发起端，B作为响应端（A call B，假设服务器和A、B已经连接好，并且只提供转发功能，PeerConnection对象为pc ）：
2.  A向B发出一个“init”请求（我觉得这步没有也行）。
3.  B收到后“init”请求后，调用pc.createOffer()方法创建一个包含SDP描述符（包含媒体信息，如分辨率、编解码能力等）的offer信令。
4.  offer信令创建成功后会调用SdpObserver监听中的onCreateSuccess()响应函数，在这里B会通过pc.setLocalDescription将offer信令（SDP描述符）赋给自己的PC对象，同时将offer信令发送给A 。
5.  A收到B的offer信令后，利用pc.setRemoteDescription()方法将B的SDP描述赋给A的PC对象。
6.  A在onCreateSuccess()监听响应函数中调用pc.setLocalDescription将answer信令（SDP描述符）赋给自己的PC对象，同时将answer信令发送给B 。
7.  B收到A的answer信令后，利用pc.setRemoteDescription()方法将A的SDP描述赋给B的PC对象。 这样，A、B之间就完成里了信令交换。

### 第三步：通过ICE框架穿透NAT/防火墙

> 如果在局域网内，信令交换后就已经可以传递媒体流了，但如果双方不在同一个局域网，就需要进行NAT/防火墙穿透。

WebRTC使用ICE框架来保证穿透。ICE全名叫交互式连接建立（Interactive Connectivity Establishment）

ICE是一种综合性的NAT/FW穿越技术，**它是一种框架**，可以整合各种NAT/FW穿越技术如STUN、TURN（Traversal Using Relay NAT 中继NAT实现的穿透）。

ICE会先使用STUN，尝试建立一个基于UDP的连接，如果失败了，就会去TCP（先尝试HTTP，然后尝试HTTPS），如果依旧失败ICE就会使用一个中继的TURN服务器。

我们可以使用Google的stun服务器：stun:stun.l.google.com:19302（但是Google需要翻墙）

> 接下来我们怎么把这个地址告诉WebRTC呢，这时候需要用到iceServers

就是在创建PeerConnection对象的时候需要的参数，iceServers里面存放的就是进行穿透地址变换的服务器地址。

添加方法如下：(保险起见可以多添加几个服务器地址，一般至少2个)

```
iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
```

> 最后这个stun服务器地址也需要通过信令交换，同样以A、B客户端为例，过程如下：

1.  A、B分别创建PC实例pc（配置了穿透服务器地址）
2.  当网络候选可用时，PeerConnection.Observer监听会调用onIceCandidate()响应函数并提供IceCandidate（里面包含穿透所需的信息）的对象。在这里，我们可以让A、B将IceCandidate对象的内容发送给对方。
3.  A、B收到对方发来的candidate信令后，利用pc.addIceCandidate()方法将穿透信息赋给各自的PeerConnection对象。

> 至此，连接通道完全打通，然后我们只需要将之前获取的媒体流localMS赋给pc即可:

```
pc.addStream(localMS);//也可以先添加，连接通道打通后一样会触发监听响应。
```

在连接通道正常的情况下，对方的PeerConnection.Observer监听就会调用onAddStream()响应函数并提供接收到的媒体流。
