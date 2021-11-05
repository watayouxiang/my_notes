## MultiDex用法-minSdkVersion<21时

- app module build.gradle

```
android {
    defaultConfig {
        minSdkVersion 16
        multiDexEnabled true
    }
}

dependencies {
    implementation 'androidx.multidex:multidex:2.0.0'
}
```

- AndroidManifest.xml - Application
  
  - 1）没有自定义Application时
  
    - ```
          <?xml version="1.0" encoding="utf-8"?>
          <manifest xmlns:android="http://schemas.android.com/apk/res/android"
              package="com.watayouxiang.androiddemo">
          
              <application
                  android:allowBackup="true"
                  android:icon="@mipmap/ic_launcher"
                  android:label="@string/app_name"
                  android:roundIcon="@mipmap/ic_launcher_round"
                  android:supportsRtl="true"
                  android:name="androidx.multidex.MultiDexApplication"
                  android:theme="@style/Theme.Android">
              
              </application>
              
          </manifest>
      ```
  
  - 2）有自定义Application：方法一
  
    - ```
      class App : MultiDexApplication() {
          override fun onCreate() {
              super.onCreate()
      
              if (ProcessUtils.isMainProcess()){
                  initInternal()
              }
          }
      
          private fun initInternal() {
              CrashUtils.init()
              Router.init()
          }
      }
      ```

  - 3）有自定义Application：方法二

    - ```
      class MyApp : Application() {
      
          override fun attachBaseContext(base: Context?) {
              super.attachBaseContext(base)
              MultiDex.install(this)
          }
      
      }
      ```



## MultiDex用法-minSdkVersion>=21时

- app module build.gradle

```
android {
    defaultConfig {
        minSdkVersion 21
        multiDexEnabled true
    }
}
```

