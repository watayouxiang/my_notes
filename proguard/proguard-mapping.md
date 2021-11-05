## mapping.txt

混淆后的代码有bug，如果定位到源码所在位置

- 可以通过 build/outputs/mapping/mapping.txt
- mapping.txt：为混淆后的代码与原来的代码的映射



## 通过命令方式翻译“混淆后的代码”

- ```
  // 进入 android sdk 目录
  $ cd /Users/TaoWang/Library/Android/sdk
  
  // 进入proguard目录
  // 使用该目录内的 retrace.sh
  // 通过 mapping.txt 将 “混淆后的代码” 批量转换成 “源码”
  $ cd /tools/proguard/bin
  
  // 假设 待转换的混淆代码文件 是 stacktrace.txt
  // 用 retrace.sh 工具，根据 mapping.txt 将 stacktrace.txt 翻译成源码
  $ ./retrace.sh -verbose [mapping.txt的绝对路径] [stacktrace.txt的绝对路径]
  
  ```