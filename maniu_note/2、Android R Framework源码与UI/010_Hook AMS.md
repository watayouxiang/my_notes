> 源码在线查阅网站：
>
> http://androidxref.com/9.0.0_r3/
>
> http://aospxref.com/android-9.0.0_r61/xref/frameworks/

## hook技术知识点

- 安卓中的hook点
  - 静态变量
  - 静态方法

- 什么是hook技术
  - 通过反射技术，来还原一个系统类，来实现特定的功能

- hook技术能做什么业务
  - 插件化
  - 换肤
  - 热修复：tinker核心就是hook
  - 组件化
  - 监听：通过 hook AMS 监听Activity生命周期

## Hook AMS中的startActivity方法实现集中式登录

- hook 安卓的AMS（基于安卓9.0版本）
  - 配合动态代理
- hook 安卓系统的Handler（基于安卓9.0版本）

### 运行环境

<img src="008_Hook AMS.assets/image-20220506211024282.png" alt="image-20220506211024282" style="zoom:50%;" />

```
distributionUrl=https\://services.gradle.org/distributions/gradle-6.5-all.zip

classpath "com.android.tools.build:gradle:4.1.2"

targetSdk 28
```

### AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.watayouxiang.demo.androidhook">

    <application
        android:name=".MyApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.AndroidHookDemo">
        <activity
            android:name=".activity.MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        <activity android:name=".activity.ProxyActivity" />
    </application>

</manifest>
```

### MainActivity.java

```java
package com.watayouxiang.demo.androidhook.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.watayouxiang.demo.androidhook.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void jump1(View view) {
        Intent intent = new Intent(this, OneActivity.class);
        startActivity(intent);
    }

    public void jump2(View view) {
        Intent intent = new Intent(this, TwoActivity.class);
        startActivity(intent);
    }

    public void logout(View view) {
        SharedPreferences share = this.getSharedPreferences("test", MODE_PRIVATE);
        SharedPreferences.Editor editor = share.edit();
        editor.putBoolean("login",false);
        editor.commit();
        Toast.makeText(this, "退出登录成功", Toast.LENGTH_SHORT).show();
    }
}

<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <Button
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:onClick="jump1"
        android:text="跳转界面1" />

    <Button
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:onClick="jump2"
        android:text="跳转界面2" />

    <Button
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:onClick="logout"
        android:text="退出登录" />
    
</LinearLayout>
```

### LoginActivity.java

```java
package com.watayouxiang.demo.androidhook.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.watayouxiang.demo.androidhook.R;

public class LoginActivity extends Activity {
    EditText name;
    EditText password;
    private String className;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        name = (EditText) findViewById(R.id.name);
        password = (EditText) findViewById(R.id.password);

        className = getIntent().getStringExtra("extraIntent");
        if (className != null) {
            ((TextView) findViewById(R.id.text)).setText(" 跳转界面：" + className);
        }
    }

    public void login(View view) {
        if ((name.getText() == null || password.getText() == null)) {
            Toast.makeText(this, "请填写用户名或密码", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences share = this.getSharedPreferences("test", MODE_PRIVATE);
        SharedPreferences.Editor editor = share.edit();
        if ("test".equals(name.getText().toString())
                && "123456".equals(password.getText().toString())) {
            editor.putBoolean("login", true);
            editor.commit();
            Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();

            if (className != null) {
                ComponentName componentName = new ComponentName(this, className);
                Intent intent = new Intent();
                intent.setComponent(componentName);
                startActivity(intent);
            }

            finish();
        } else {
            editor.putBoolean("login", false);
            editor.commit();
            Toast.makeText(this, "登录失败", Toast.LENGTH_SHORT).show();
        }
    }
}

<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <TextView
        android:id="@+id/text"
        android:layout_width="match_parent"
        android:layout_height="50dp"
        android:gravity="center" />

    <EditText
        android:id="@+id/name"
        android:layout_width="match_parent"
        android:layout_height="40dp"
        android:layout_marginLeft="10dp"
        android:hint="  用户名"
        android:textSize="15sp" />

    <EditText
        android:id="@+id/password"
        android:layout_width="match_parent"
        android:layout_height="40dp"
        android:layout_marginLeft="10dp"
        android:hint="  密码"
        android:textSize="15sp" />

    <Button
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="20dp"
        android:gravity="center"
        android:onClick="login"
        android:padding="10dp"
        android:text="登录"
        android:textColor="#fff"
        android:textSize="18sp" />
</LinearLayout>
```

### 其他Activity

```java
package com.watayouxiang.demo.androidhook.activity;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.watayouxiang.demo.androidhook.R;

public class OneActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity_one);
    }
}

<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <TextView
        android:layout_width="wrap_content"
        android:layout_centerInParent="true"
        android:text="第一个需要登录的页面"
        android:layout_height="wrap_content"/>

</RelativeLayout>
```

```java
package com.watayouxiang.demo.androidhook.activity;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.watayouxiang.demo.androidhook.R;

public class TwoActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity_two);
    }
}

<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <TextView
        android:layout_width="wrap_content"
        android:layout_centerInParent="true"
        android:text="第二个需要登录的页面"
        android:layout_height="wrap_content"/>

</RelativeLayout>
```

```java
package com.watayouxiang.demo.androidhook.activity;

import android.app.Activity;

public class ProxyActivity extends Activity {
}
```

### MyApp.java

```java
package com.watayouxiang.demo.androidhook;

import android.app.Application;

import com.watayouxiang.demo.androidhook.activity.ProxyActivity;
import com.watayouxiang.demo.androidhook.hook.HookUtils;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        HookUtils hookUtils = new HookUtils(this, ProxyActivity.class);
        try {
            hookUtils.hookAms();
            hookUtils.hookSystemHandler();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### HookUtils.java

```java
package com.watayouxiang.demo.androidhook.hook;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;

import com.watayouxiang.demo.androidhook.activity.LoginActivity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * 只有运行在9.0的系统才有效
 */
public class HookUtils {
    private final Context context;
    private final Class<?> proxyActivity;

    public HookUtils(Context context, Class<?> proxyActivity) {
        this.context = context;
        this.proxyActivity = proxyActivity;
    }

    // ====================================================================================
    // hook 修改 startActivity
    // ====================================================================================

    public void hookAms() throws Exception {
        // 1、拿到 Singleton<IActivityManager> IActivityManagerSingleton
        Class ActivityManagerClz = Class.forName("android.app.ActivityManager");
        Field IActivityManagerSingletonFiled = ActivityManagerClz.getDeclaredField("IActivityManagerSingleton");
        IActivityManagerSingletonFiled.setAccessible(true);
        Object IActivityManagerSingletonObj = IActivityManagerSingletonFiled.get(null);

        // 2、拿到 IActivityManager
        // /frameworks/base/core/java/android/app/IActivityManager.aidl
        Class SingletonClz = Class.forName("android.util.Singleton");
        Field mInstanceField = SingletonClz.getDeclaredField("mInstance");
        mInstanceField.setAccessible(true);
        Object IActivityManagerObj = mInstanceField.get(IActivityManagerSingletonObj);
        Class IActivityManagerClz = Class.forName("android.app.IActivityManager");

        // 3、动态代理 IActivityManager
        Object proxyIActivityManager = Proxy.newProxyInstance(
                // 类加载器
                Thread.currentThread().getContextClassLoader(),
                // 需要被代理的类
                new Class[]{IActivityManagerClz},
                // 代理
                new AmsInvocationHandler(IActivityManagerObj)
        );
        mInstanceField.set(IActivityManagerSingletonObj, proxyIActivityManager);
    }

    private class AmsInvocationHandler implements InvocationHandler {
        private final Object iActivityManagerObject;

        public AmsInvocationHandler(Object iActivityManagerObject) {
            this.iActivityManagerObject = iActivityManagerObject;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("startActivity".contains(method.getName())) {
                Intent intent = null;
                int index = 0;
                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    if (arg instanceof Intent) {
                        intent = (Intent) args[i];
                        index = i;
                        break;
                    }
                }
                Intent proxyIntent = new Intent();
                ComponentName componentName = new ComponentName(context, proxyActivity);
                proxyIntent.setComponent(componentName);
                proxyIntent.putExtra("oldIntent", intent);
                args[index] = proxyIntent;
            }
            return method.invoke(iActivityManagerObject, args);
        }
    }

    // ====================================================================================
    // hook 修改 Handler
    // ====================================================================================

    public void hookSystemHandler() throws Exception {
        // 1、拿到 ActivityThread
        Class ActivityThreadClz = Class.forName("android.app.ActivityThread");
        Field field = ActivityThreadClz.getDeclaredField("sCurrentActivityThread");
        field.setAccessible(true);
        Object ActivityThreadObj = field.get(null);

        // 2、拿到系统 Handler
        Field mHField = ActivityThreadClz.getDeclaredField("mH");
        mHField.setAccessible(true);
        Handler mHObj = (Handler) mHField.get(ActivityThreadObj);

        // 3、修改 Handler 的 mCallback
        Field mCallbackField = Handler.class.getDeclaredField("mCallback");
        mCallbackField.setAccessible(true);
        ProxyHandlerCallback proxyMHCallback = new ProxyHandlerCallback();
        mCallbackField.set(mHObj, proxyMHCallback);

    }

    private class ProxyHandlerCallback implements Handler.Callback {
        private int EXECUTE_TRANSACTION = 159;

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == EXECUTE_TRANSACTION) {
                try {
                    Class ClientTransactionClz = Class.forName("android.app.servertransaction.ClientTransaction");
                    if (!ClientTransactionClz.isInstance(msg.obj)) return false;

                    Field mActivityCallbacksField = ClientTransactionClz.getDeclaredField("mActivityCallbacks");
                    mActivityCallbacksField.setAccessible(true);
                    Object mActivityCallbacksObj = mActivityCallbacksField.get(msg.obj);

                    List list = (List) mActivityCallbacksObj;
                    if (list.size() == 0) return false;

                    Object LaunchActivityItemObj = list.get(0);
                    Class LaunchActivityItemClz = Class.forName("android.app.servertransaction.LaunchActivityItem");
                    if (!LaunchActivityItemClz.isInstance(LaunchActivityItemObj)) return false;

                    Field mIntentField = LaunchActivityItemClz.getDeclaredField("mIntent");
                    mIntentField.setAccessible(true);
                    Intent mIntent = (Intent) mIntentField.get(LaunchActivityItemObj);
                    Intent realIntent = mIntent.getParcelableExtra("oldIntent");
                    if (realIntent != null) {
                        SharedPreferences preferences = context.getSharedPreferences("test", Context.MODE_PRIVATE);
                        if (preferences.getBoolean("login", false)) {
                            mIntent.setComponent(realIntent.getComponent());
                        } else {
                            mIntent.putExtra("extraIntent", realIntent.getComponent().getClassName());
                            ComponentName componentName = new ComponentName(context, LoginActivity.class);
                            mIntent.setComponent(componentName);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
    }
}
```
