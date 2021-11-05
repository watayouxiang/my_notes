### gradle 报错了，怎么排查错误？



通过 Terminal 查看  gradle 的所有 debug 日志：

 ```
$ pwd
/Users/TaoWang/Documents/tiocloud/tio-chat-android

$ ls
README.md               build.gradle            gradlew                 lib-httpclient          lib-webrtclib           tio-chat-android.iml
app                     gradle                  gradlew.bat             lib-imclient            local.properties        uikit
build                   gradle.properties       key                     lib-utilcode            settings.gradle

// 查看 gradle 的所有日志，包含了错误日志。
$ ./gradlew --debug
......

 ```



