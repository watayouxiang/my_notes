# dex反编译



## 1、解压apk，获取dex文件

## 2、dex2jar转dex为jar

dex2jar下载地址：https://sourceforge.net/projects/dex2jar/files/latest/download

```
$ cd /Users/TaoWang/Downloads

// 解压 TioIm-release-V2.4.5.apk 到 apk 目录
// -n：解压缩时不要覆盖原有的文件
// -d<目录>：指定文件解压缩后所要存储的目录
$ unzip -n TioIm-release-V2.4.5.apk -d apk

// 解压 dex2jar-2.0.zip 到 当前目录
// -n：解压缩时不要覆盖原有的文件
$ unzip -n dex2jar-2.0.zip

// 获取 d2j-dex2jar.sh 的执行权限
$ chmod 755 dex2jar-2.0/d2j-dex2jar.sh

// 获取 d2j_invoke.sh 的执行权限
$ chmod 755 dex2jar-2.0/d2j_invoke.sh

// 用 d2j-dex2jar.sh 将 classes.dex 转成jar文件
// jar文件输出目录：/Users/TaoWang/Downloads/classes-dex2jar.jar
$ dex2jar-2.0/d2j-dex2jar.sh apk/classes.dex
```



## 3、jd-gui查看jar

jd-gui 下载地址：https://github.com/java-decompiler/jd-gui/releases/

Mac升级Big Sur后打开JD-GUI发生报错该如何解决：https://www.zhihu.com/question/430115210/answer/1577179374