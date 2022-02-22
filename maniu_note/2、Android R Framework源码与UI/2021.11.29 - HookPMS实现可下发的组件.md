## 本节课内容

1. 提前混存缩略存档
2. 四大组件的预加载
3. AMS与PMS通信解析
4. APK安装过程分析、PMS对安装包的解析原理
5. 利用PMS原理实现可下发的广播组件

## 插件化/热修复

> Hook PMS 实现可下发的组件，就是插件化（或者“热修复”）的前身。

- 下载apk
- 解析apk
- 加载组件

## 利用PMS原理实现可下发的广播组件

1. 事先写好广播组件，并打包成apk文件，上传到sdcard目录中
2. 通过反射获取 PMS 的 parsePackage 方法，调用该方法从而得到 package 对象，再进一步拿到 receivers 列表
3. 遍历 receives，通过 dexClassLoader 根据 receiver 全类名，实例化出具体的 receiver 对象
4. 通过反射拿到 android.content.pm.PackageParser$Component 中的 intents 对象
5. 根据第三步和第四步得到的对象，调用 context.registerReceiver(receiver, filter) 实现 apk 文件中的广播注册

