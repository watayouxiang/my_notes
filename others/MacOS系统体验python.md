# MacOS系统体验python(20190111) #

> 作者操作系统 macOS Mojava 10.14，该系统自带 python 环境，版本号为 2.7
>
> 可通过 `$ python -V` 命令查询

## pythoy官网下载并安装最新环境 ##

官网 [https://www.python.org/](https://www.python.org/) 

Python>>>  Downloads>>>  Mac OS X 下载如下版本：

	Python 3.7.2 - 2018-12-24
	Download macOS 64-bit installer

安装完成后会出现以下两个APP

![WX20190111-142808@2x.png](https://upload-images.jianshu.io/upload_images/1490226-1c0eaed9d5e56b4a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

IDLE 是图形化界面，Python Launcher 可以配置python

![WX20190111-143547@2x.png](https://upload-images.jianshu.io/upload_images/1490226-987639e80aa08708.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

用 IDLE.app 输出“hello world” （该编辑器关键字智能提示键为 `tab` ）

## 切换macOS默认使用的 Python 版本 ##

macOS自带的python 2.7版本，但是如果想终端默认运行的是新下载的python 3.7.2版本可以这么做：

1. 首先找到3.7.2版本python的路径  `/Library/Frameworks/Python.framework/Versions/3.7/bin/python3.7`
2. 打开 `~/.bash_profile` 目录下的隐藏文件 `.bash_profile`，新增一行 `alias python="/Library/Frameworks/Python.framework/Versions/3.7/bin/python3.7"`
3. 重启 Terminal 生效


## 使用Anaconda玩耍python ##

Anaconda是学习Python很好的一个平台，所以可以用Anaconda-Navigator.app玩耍python

官网 [https://www.anaconda.com/what-is-anaconda/](https://www.anaconda.com/what-is-anaconda/)

安装完成后会出现如下APP

![WX20190111-144708@2x.png](https://upload-images.jianshu.io/upload_images/1490226-77432a2c35b292b7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

Anaconda-Navigator.app 界面如下

![WX20190111-150206@2x.png](https://upload-images.jianshu.io/upload_images/1490226-718a2620ca5d9408.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

Anaconda提供多种python编辑器，这里以Jupyter Notebook编辑器为例，点击运行

![WX20190111-150704@2x.png](https://upload-images.jianshu.io/upload_images/1490226-fae5c77d01aa03fd.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

Jupyter Notebook 编辑器如下

![WX20190111-153233@2x.png](https://upload-images.jianshu.io/upload_images/1490226-66a3da4ddb0c7f6c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

```
import json
import requests

url = 'https://oapi.dingtalk.com/robot/send?access_token=xxx'

HEADERS = {"Content-Type":"application/json;charset=utf-8"}

String_textMsg = {
    "msgtype": "link", 
    "link": {
        "text":"群机器人是钉钉群的高级扩展功能。群机器人可以将第三方服务的信息聚合到群聊中，不信你也可以创建个来玩玩", 
       "title": "自定义机器人协议", 
        "picUrl": "", 
        "messageUrl": "https://open-doc.dingtalk.com/docs/doc.htm?spm=a219a.7629140.0.0.Rqyvqo&treeId=257&articleId=105735&docType=1"
    }
}
                  
String_textMsg = json.dumps(String_textMsg)

res = requests.post(url,data=String_textMsg,headers=HEADERS)

print(res.text)
```

- shitft+enter：运行py代码
- tab：关键字智能提示
- python语法学习网站推荐 W3C：[https://www.w3cschool.cn/python3/python3-basic-syntax.html](https://www.w3cschool.cn/python3/python3-basic-syntax.html)




