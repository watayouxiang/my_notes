### 引入Kotlin步骤



1、project 目录下的 `build.gradle` 添加 kotlin 插件

```groovy
buildscript {
    // gradle 插件
    dependencies {
        // 添加 kotlin 编译插件
        classpath 'org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.72'
    }
}
```

2、工程目录下的 `build.gradle` 应用 kotlin 插件

```groovy
apply plugin: 'com.android.library'
// 应用 kotlin 插件
apply plugin: 'kotlin-android'
```

3、工程目录下的 `build.gradle` 引入 kotlin 依赖

```groovy
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    // 添加 kotlin 标准库依赖
    implementation 'org.jetbrains.kotlin:kotlin-stdlib:1.3.72'
}
```

