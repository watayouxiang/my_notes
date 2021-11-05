## 混淆的规则

```java
# ---------------------
  
1.压缩 Shrinking
  
# 默认开启，优化Apk体积，移除未使用的类和成员
# 关闭压缩
-dontshrink

# ---------------------
  
2.优化 Optimization
  
# 默认开启，在字节码中进行优化，让应用运行更快
# 关闭优化
-dontoptimize

# 表示proguard对代码进行迭代优化的次数，Android一般为5
-optimizationpasses n  

# ---------------------
  
3.混淆 Obfuscation
  
# 默认开启，类和成员随机命名，增加反编译及阅读难度，可以使用keep命令保存
# 关闭混淆
- dontobfuscate

# ---------------------

4.keep

#表示只是保持该包下的类名，而子包下的类名还是会被混淆
-keep class com.android.xx.*
#两颗星表示把本包和所含子包下的类名都保持
-keep class com.android.xx.**
#既可以保持该包下的类名，又可以保持类里面的内容不被混淆;
-keep class com.android.xx.*{*;}
#既可以保持该包及子包下的类名，又可以保持类里面的内容不被混淆;
-keep class com.android.xx.**{*;}
#保持某个类名不被混淆，但是内部内容会被混淆
-keep class com.android.Activity
#保持某个类的 类名及内部的所有内容不会混淆
-keep class com.android.Activity{*;}
#保持类中特定内容，而不是所有的内容可以使用如下
-keep class com.android.Activity {
  #匹配所有构造器
  <init>;
  #匹配所有域
  <fields>;
  #匹配所有方法
  <methods>;
}
#还可以进一步的优化
-keep class com.android.Activity{
  #保持该类下所有的共有方法不被混淆
  public <methods>;
  #保持该类下所有的共有内容不被混淆
  public *;
  #保持该类下所有的私有方法不被混淆
  private <methods>;
  #保持该类下所有的私有内容不被混淆
  private *;
  #保持该类的String类型的构造方法
  public <init>(java.lang.String);
}
#要保留一个类中的内部类不被混淆需要用 $ 符号
-keep class com.android.Activity$TestClass{*;}
#保持继承关系不被混淆
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.view.View
#不需要保持类名可以使用keepclassmembers

# ---------------------
  
5.不可混淆
 
a.JNI不可混淆
  
#保持native方法不被混淆
-keepclasseswithmembernames class * {    
  native <methods>; 
}

b.清单文件

清单文件中所有使用到的类以及所有Android SDK下的类都无需混淆

c.自定义View

需要保持自定义View不混淆

d.Json对象类

需要保持Json对象类不混淆

e.第三方

正规的第三方都会提供混淆规则的

f.Parcelable的子类和Creator静态成员变量

Parcelable的子类和Creator静态成员变量不混淆，否则会产生Android.os.BadParcelableException异常；
  
-keep class * implements Android.os.Parcelable { 
  # 保持Parcelable不被混淆            
  public static final Android.os.Parcelable$Creator *;
}

g.枚举
  
#需要保持
-keepclassmembers enum * { 
 public static **[] values();
 public static ** valueOf(java.lang.String);
}

# 包名不混淆
-keeppackagenames com.watayouxiang.*
  
# 不要混淆某类的某个方法
-keepclasseswithmembers class com.watayouxiang.demo.MainActivity {
	private void importantMethod();
}
```



## 混淆配置

```
// 打包配置
buildTypes {
    release {
    	// 清理无用资源
    	shrinkResources true
    	// 是否启动ZipAlign压缩
    	zipAlignEnabled true
    	// 是否混淆
    	minifyEnabled true
    	// 签名
    	signingConfig signingConfigs.config
    	// 混淆规则文件
    	proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
    	proguardFiles getDefaultProguardFile('proguard-android.txt')
    }
    debug {
    	signingConfig signingConfigs.config
    }
}
```


