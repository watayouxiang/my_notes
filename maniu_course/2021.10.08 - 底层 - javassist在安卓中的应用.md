## javassist是什么

**定义：**

- javaassist 就是一个用来处理 Java 字节码的类库。

- javassist 也称为动态编译，动态编译技术通过操作 class 文件，动态添加元素或者修改代码。

- 可应用于组件化、热修复、增量升级、AndroidStudio插件

**apk打包过程：**

java源码 --> class --> dex文件  --> apk

- APT，aspectj 作用在：java源码 --> class 期间

- javassit 作用在：class --> dex文件 期间

## /build/intermediates 文件夹讲解

- /build/intermediates
  - javac：java 编译成 class 文件
  - transforms：class 编译成 dex 文件
  - merged：dex和一系列文件一起打包成 apk 文件
    - merged_assets
    - merged_java_res
    - merged_jni_libs
    - merged_manifest
    - merged_manifests
    - merged_native_libs
    - merged_res_blame_folder
    - merged_shaders
  - dex：由transforms中的多个dex合并
- /build/outputs：输出apk文件

## javassist的简单使用

### 1）创建 javassist 工程

1、新建一个 groovy library（通过修改 java library 而来）

2、module build.gradle 如下

```groovy
apply plugin: 'groovy'
apply plugin: 'maven'

repositories {
    mavenCentral()
}

dependencies {
    //gradle sdk
    implementation gradleApi()
    //groovy sdk
    implementation localGroovy()
    implementation 'com.android.tools.build:gradle:3.1.3'
    implementation 'org.javassist:javassist:3.20.0-GA'
}
```

3、删除目录 src/main/java，创建目录 src/main/groovy

4、新建 ModifyPlugin.groovy

```groovy
package com.watayouxiang.javassist.test


import org.gradle.api.Plugin
import org.gradle.api.Project

class ModifyPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        println "---------------------> ModifyPlugin"
    }
}
```

5、注册插件 ModifyPlugin.groovy

新建 src/main/resources/META-INF/gradle-plugins/com.watayouxiang.javassist.test.properties

其中 com.javassist.properties 中的 com.javassist 就是插件的 id

```properties
implementation-class=com.watayouxiang.javassist.test.ModifyPlugin
```

### 2）javassist项目打jar包并使用

javassist 项目打 jar 包

```groovy
// javassist 项目的 module gradle 添加如下：
uploadArchives{
    repositories.mavenDeployer {
        pom.groupId = 'com.watayouxiang.javassist.test'
        pom.artifactId = 'modify'
        pom.version = '1.0.0'
        repository(url: uri('../repo'))
    }
}

// 执行 gradle 任务面板中的 javassist > Tasks > upload > uploadArchives 打成 jar 包
```

使用 javassist 本地 jar 包

```groovy
// project gradle 文件添加
buildscript {
    repositories {
        maven{
            url uri('repo')
        }
    }
    dependencies {
        classpath "com.watayouxiang.javassist.test:modify:1.0.0"
    }
}

// app gradle 文件添加
apply plugin: 'com.watayouxiang.javassist.test'
```

### 3）使用transform

```groovy
package com.watayouxiang.javassist.test

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

class ModifyTransform extends Transform {

    def project

    // ClassPool 是 CtClass 对象的容器。
    // 需要注意的是 ClassPool 会在内存中维护所有被它创建过的 CtClass，当 CtClass 数量过多时，会占用大量的内存，
    // API中给出的解决方案是，有意识的调用 CtClass 的 detach() 方法以释放内存。
    def pool = ClassPool.default

    ModifyTransform(Project project) {
        this.project = project
    }

    /**
     * transforms 下文件夹的名称
     */
    @Override
    String getName() {
        return "watayouxiang"
    }

    /**
     * 输入类型
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * 范围
     */
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    /**
     * 处理 class 文件
     */
    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)

        project.android.bootClasspath.each {
            pool.appendClassPath(it.absolutePath)
        }

        transformInvocation.inputs.each {

            // 1、拿到 DirectoryInput 类型的输入
            it.directoryInputs.each {
                def preFileName = it.file.absolutePath
                pool.insertClassPath(preFileName)
                println "DirectoryInput ----------------> " + preFileName
                findTarget(it.file, preFileName)
                // 2、获取输出的文件夹
                def dest = transformInvocation.outputProvider.getContentLocation(
                        it.name,
                        it.contentTypes,
                        it.scopes,
                        Format.DIRECTORY
                )
                // 3、将输入文件拷贝到输出文件夹
                FileUtils.copyDirectory(it.file, dest)
            }

            // 1、拿到 JarInput 类型的输入
            it.jarInputs.each {
                // 2、获取输出的文件夹
                def dest = transformInvocation.outputProvider.getContentLocation(
                        it.name,
                        it.contentTypes,
                        it.scopes,
                        Format.JAR
                )
                // 3、将输入文件拷贝到输出文件夹
                FileUtils.copyFile(it.file, dest)
            }
        }
    }

    // 查找 .class 文件
    private void findTarget(File dir, String fileName) {
        if (dir.isDirectory()) {
            dir.listFiles().each {
                findTarget(it, fileName)
            }
        } else {
            def filePath = dir.absolutePath
            if (filePath.endsWith(".class")) {
                modify(filePath, fileName)
            }
        }
    }

    // 修改 .class 文件
    private void modify(def filePath, String fileName) {
        // 过滤这些文件
        if (filePath.contains('R$') || filePath.contains('R.class')
                || filePath.contains("BuildConfig.class")) {
            return
        }

        // 获取 .class 的文件名
        def className = filePath.replace(fileName, "").replace("\\", ".").replace("/", ".")
        def name = className.replace(".class", "").substring(1)

        // /Users/TaoWang/Desktop/javassist_demo/javassist_android_demo/app/build/intermediates/javac/debug/classes/com/watayouxiang/javassistdemo/MainActivity.class
        println "filePath -------------> " + filePath
        // /Users/TaoWang/Desktop/javassist_demo/javassist_android_demo/app/build/intermediates/javac/debug/classes
        println "fileName -------------> " + fileName
        // com.watayouxiang.javassistdemo.MainActivity
        println "name -------------> " + name

        // 给 .class 文件添加代码
        CtClass ctClass = pool.get(name)
        addCode(ctClass, fileName)
    }

    // 给 .class 文件添加代码
    private void addCode(CtClass ctClass, String fileName) {
        // 开始使用 javassist
        // 捡出来
        ctClass.defrost()
        // 获取所有方法
        CtMethod[] methods = ctClass.getDeclaredMethods()
        for (method in methods) {
            println "---------------> method: " + method.getName() + ", 参数个数: " + method.getParameterTypes().length
            method.insertAfter("if(true){}")
            if (method.getParameterTypes().length == 1) {
                method.insertBefore("{ System.out.println(\$1);}")
            }
            if (method.getParameterTypes().length == 2) {
                method.insertBefore("{ System.out.println(\$1); System.out.println(\$2);}")
            }
            if (method.getParameterTypes().length == 3) {
                method.insertBefore("{ System.out.println(\$1); System.out.println(\$2); System.out.println(\$3);}")
            }
        }
        // 将修改后的代码写回去
        ctClass.writeFile(fileName)
        // 释放资源
        ctClass.detach()
    }
}
```

```groovy
package com.watayouxiang.javassist.test

import org.gradle.api.Plugin
import org.gradle.api.Project

class ModifyPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        println "---------------------> ModifyPlugin"
        project.android.registerTransform(new ModifyTransform(project))
    }
}
```



