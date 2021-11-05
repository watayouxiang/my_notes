# charles对安卓抓包(mac版) #

> 20190111



## 1. 设置macOS版charles代理 ##

Charles 打开：Proxy / Proxy Settings -> HTTP Proxy选项卡

- 设置端口号 8888
- 并勾选 Enable transparent HTTP proxying 选项

<img src="https://upload-images.jianshu.io/upload_images/1490226-bf139871de5028e4.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240" width="600" />

## 2. 查看charles连接手机代理的帮助 ##

Charles 打开：Help / SSL Proxying / Insatll Charles Root Certificate on a Mobile Device or Remote Browser

- 先提示请设置手机代理到 192.168.123.26:8888
- 然后浏览器打开 chls.pro/ssl 下载并安装证书

<img src="https://upload-images.jianshu.io/upload_images/1490226-10266f033c4e7186.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240" width="600" />

<img src="https://upload-images.jianshu.io/upload_images/1490226-b0c8243b62ca44b2.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240" width="600" />

## 3. 安卓手机连接charles代理 ##

安卓手机打开：设置 -> WLAN -> 修改网络 -> 显示高级选项

- 设置代理为 “手动代理”
- ip 设置 “192.168.123.26”
- port 设置 “8888”

<img src="https://upload-images.jianshu.io/upload_images/1490226-e6fd4cb54b4650e0.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240" width="300" />

## 安卓手机下载并安装charles证书（待续...） ##
## charles抓取安卓手机https包（待续...） ##
