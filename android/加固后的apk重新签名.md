# 加固后的apk重新签名

> 参考：https://blog.csdn.net/snowbeatrain/article/details/105275092



具备java环境，然后运行以下命令：



```
jarsigner -digestalg SHA1 -sigalg MD5withRSA -verbose 

# 你的keystore文件路径
-keystore /Users/TaoWang/Documents/Code/tiocloud/android/sign/tiochat_wt2020.keystore

# 重新签名后生成的apk文件路径
-signedjar /Users/TaoWang/Desktop/tioIm.apk 

# 要加固的apk文件路径
/Users/TaoWang/Desktop/08c6c7d09472a5e57f9e6f23edba2417.20210730112558.apk

# 你的keystore文件别名
tiochat_wt2020
```



```
jarsigner -digestalg SHA1 -sigalg MD5withRSA -verbose -keystore /Users/TaoWang/Documents/Code/tiocloud/android/sign/tiochat_wt2020.keystore -signedjar /Users/TaoWang/Desktop/tioIm.apk /Users/TaoWang/Desktop/08c6c7d09472a5e57f9e6f23edba2417.20210730112558.apk tiochat_wt2020
```




jarsigner -digestalg SHA1 -sigalg MD5withRSA -tsa -verbose -keystore [你的keystore文件路径] -signedjar [重新签名后生成的apk文件路径] [要加固的apk文件路径] [你的keystore文件别名]

