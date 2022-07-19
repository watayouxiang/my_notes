> 源码在线查阅网站：http://aospxref.com/android-9.0.0_r61/xref/frameworks/
>

## 一、插件化/热修复

> Hook PMS 实现可下发的组件，就是插件化（或者“热修复”）的前身。

- 下载apk
- 解析apk
- 加载组件

## 二、利用PMS原理实现可下发的广播组件

1. 事先写好广播组件，并打包成apk文件，上传到sdcard目录中
2. 通过反射获取 PMS 的 parsePackage 方法，调用该方法从而得到 package 对象，再进一步拿到 receivers 列表
3. 遍历 receives，通过 dexClassLoader 根据 receiver 全类名，实例化出具体的 receiver 对象
4. 通过反射拿到 android.content.pm.PackageParser$Component 中的 intents 对象
5. 根据第三步和第四步得到的对象，调用 context.registerReceiver(receiver, filter) 实现 apk 文件中的广播注册

### 开发环境

```
// app
targetSdkVersion 28

// rooot build.gradle
classpath "com.android.tools.build:gradle:4.1.2"

// gradle
distributionUrl=https\://services.gradle.org/distributions/gradle-6.5-bin.zip
```

### app（宿主）

#### MainActivity

```
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
```

```java
package com.maniu.pmsmaniu;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.maniu.receiver2");
        registerReceiver(new PersonPickerReceiver(), filter);
    }

    private static void checkPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    public void registerBroaderCast(View view) {
        try {
            File file = new File(Environment.getExternalStorageDirectory(), "input.apk");
            new MyPackageParser().parserReceivers(this,file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendBroaderCast(View view) {
        Toast.makeText(this, "1、宿主：准备发送广播", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.setAction("com.maniu.receiver1");
        sendBroadcast(intent);
    }

    static class PersonPickerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "3、宿主：三次握手完成", Toast.LENGTH_SHORT).show();
        }
    }

}
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context=".MainActivity">

    <Button
        android:layout_width="match_parent"
        android:layout_height="50dp"
        android:gravity="center"
        android:onClick="registerBroaderCast"
        android:text="注册广播" />

    <Button
        android:layout_width="match_parent"
        android:layout_height="50dp"
        android:gravity="center"
        android:onClick="sendBroaderCast"
        android:text="发送广播" />

</LinearLayout>
```

#### MyPackageParser


```java
package com.maniu.pmsmaniu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import dalvik.system.DexClassLoader;

public class MyPackageParser {
    /**
     * 只能运行在 Android 9.0
     */
    public void parserReceivers(Context context, File apkFile) throws Exception {
        // http://aospxref.com/android-9.0.0_r61/xref/frameworks/base/core/java/android/content/pm/PackageParser.java
        Class<?> packageParserClass = Class.forName("android.content.pm.PackageParser");
        // Package package = PackageParser#parsePackage(File, int)
        // line 1033
        Method parsePackageMethod = packageParserClass.getDeclaredMethod("parsePackage", File.class, int.class);
        parsePackageMethod.setAccessible(true);
        Object packageParser = packageParserClass.newInstance();
        // Package 为 PackageParser 的内部类
        // line 6193
        Object packageObj = parsePackageMethod.invoke(packageParser, apkFile, PackageManager.GET_RECEIVERS);
        // public final ArrayList<Activity> receivers = new ArrayList<Activity>(0);
        // line 6243
        Field receiversField = packageObj.getClass().getDeclaredField("receivers");
        List receivers = (List) receiversField.get(packageObj);
        // DexClassLoader
        DexClassLoader dexClassLoader = new DexClassLoader(
                // String dexPath
                apkFile.getAbsolutePath(),
                // String optimizedDirectory
                context.getDir("plugin", Context.MODE_PRIVATE).getAbsolutePath(),
                // String librarySearchPath
                null,
                // ClassLoader parent
                context.getClassLoader()
        );
        // public static abstract class Component<II extends IntentInfo> {
        // line 7057
        Class<?> componentClass = Class.forName("android.content.pm.PackageParser$Component");
        // public final ArrayList<II> intents;
        // line 7058
        Field intentsField = componentClass.getDeclaredField("intents");
        // receiverObject 是 PackageParser$Activity
        // PackageParser$Activity 在 line 7518
        for (Object receiverObject : receivers) {
            // 拿到 PackageParser$Activity 的 className 成员变量
            // line 7059
            String name = (String) receiverObject.getClass().getField("className").get(receiverObject);
            try {
                // 创建广播
                BroadcastReceiver broadcastReceiver = (BroadcastReceiver) dexClassLoader.loadClass(name).newInstance();
                // 动态注册广播
                List<? extends IntentFilter> filters = (List<? extends IntentFilter>) intentsField.get(receiverObject);
                for (IntentFilter filter : filters) {
                    context.registerReceiver(broadcastReceiver, filter);
                }
            } catch (Exception e) {
            }
        }
    }
}
```

### receiver（插件）

#### MainActivity

```java
package com.maniu.receiver;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
```

#### DavidReceiver

```
<receiver android:name=".DavidReceiver"
    android:exported="true">
    <intent-filter>
        <action android:name="com.maniu.receiver1"/>
    </intent-filter>
</receiver>
```

```java
package com.maniu.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class DavidReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "2、收到你的信息了", Toast.LENGTH_SHORT).show();
        Intent intent1 = new Intent();
        intent1.setAction("com.maniu.receiver2");
        context.sendBroadcast(intent1);
    }
}
```
