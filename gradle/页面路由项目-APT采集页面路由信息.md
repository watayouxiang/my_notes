## 1、建立router-annotations注解工程

- 建立 router-annotations 目录

- 添加 build.gradle

  - ```
    // 应用 java 插件
    apply plugin: 'java'
    
    // 设置源码兼容性
    targetCompatibility = JavaVersion.VERSION_1_7
    sourceCompatibility = JavaVersion.VERSION_1_7
    ```

- 创建注解 src/main/java/com.imooc.router.annotations.Destination

  - ```
    /**
     * 说明当前注解可以修饰的元素，此处表示可以用于标记在类上面
     */
    @Target({ElementType.TYPE})
    /**
     * 说明当前注解可以被保留的时间
     */
    @Retention(RetentionPolicy.CLASS)
    public @interface Destination {
        /**
         * 当前页面的url，不能为空
         *
         * @return 页面的url
         */
        String url();
    
        /**
         * 对于当前页面的中文描述
         *
         * @return 例如："个人主页"
         */
        String description();
    }
    ```



## 2、建立router-processor注解处理器工程

- 添加 build.gradle

  - ```
    // 引入java插件，帮助编译代码
    apply plugin: 'java'
    
    dependencies {
        implementation project(':router-annotations')
        
        // 使用google的注解处理器，@AutoService(Processor.class)
        // 会帮助自动创建 META-INF/services/javax.annotation.processing.Processor 文件
        implementation 'com.google.auto.service:auto-service:1.0-rc6'
        annotationProcessor 'com.google.auto.service:auto-service:1.0-rc6'
    }
    ```

- 创建注解处理器 src/main/java/com.imooc.router.processor.DestinationProcessor

  - ```
    /**
     * 告诉 javac 加载注解处理器 DestinationProcessor
     * <p>
     * 会帮助自动创建 META-INF/services/javax.annotation.processing.Processor 文件
     */
    @AutoService(Processor.class)
    public class DestinationProcessor extends AbstractProcessor {
    
        private static final String TAG = "DestinationProcessor";
    
        /**
         * 编译器找到我们关心的注解后，会回调这个方法
         *
         * @param set
         * @param roundEnvironment
         * @return
         */
        @Override
        public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
            // 避免多次调用 process
            if (roundEnvironment.processingOver()) {
                return false;
            }
    
            System.out.println(TAG + " >>> process start ...");
    
            // 获取所有标记了 @Destination 注解的 类的信息
            Set<Element> allDestinationElements = (Set<Element>) roundEnvironment.getElementsAnnotatedWith(Destination.class);
    
            System.out.println(TAG + " >>> all Destination elements count = " + allDestinationElements.size());
    
            // 当未收集到 @Destination 注解的时候，跳过后续流程
            if (allDestinationElements.size() < 1) {
                return false;
            }
    
            // 将要自动生成的类的类名
            String className = "RouterMapping_" + System.currentTimeMillis();
    
            StringBuilder builder = new StringBuilder();
    
            builder.append("package com.watayouxiang.androiddemo.mapping;\n\n");
            builder.append("import java.util.HashMap;\n");
            builder.append("import java.util.Map;\n\n");
            builder.append("public class ").append(className).append(" {\n\n");
            builder.append("    public static Map<String, String> get() {\n");
            builder.append("        Map<String, String> mapping = new HashMap<>();\n\n");
    
    
            final JsonArray destinationJsonArray = new JsonArray();
    
            // 遍历所有 @Destination 注解信息，挨个获取详细信息
            for (Element element : allDestinationElements) {
    
                final TypeElement typeElement = (TypeElement) element;
    
                // 尝试在当前类上，获取 @Destination 的信息
                final Destination destination = typeElement.getAnnotation(Destination.class);
    
                if (destination == null) continue;
    
                final String url = destination.url();
                final String description = destination.description();
                // 获取注解当前类的全类名
                final String realPath = typeElement.getQualifiedName().toString();
    
                System.out.println(TAG + " >>> url = " + url);
                System.out.println(TAG + " >>> description = " + description);
                System.out.println(TAG + " >>> realPath = " + realPath);
    
                builder.append("        ")
                        .append("mapping.put(")
                        .append("\"" + url + "\"")
                        .append(", ")
                        .append("\"" + realPath + "\"")
                        .append(");\n");
    
                // 组装json对象
                JsonObject item = new JsonObject();
                item.addProperty("url", url);
                item.addProperty("description", description);
                item.addProperty("realPath", realPath);
    
                destinationJsonArray.add(item);
            }
    
            builder.append("\n");
            builder.append("        return mapping;\n");
            builder.append("    }\n\n");
            builder.append("}\n");
    
            String mappingFullClassName = "com.watayouxiang.androiddemo.mapping." + className;
    
            System.out.println(TAG + " >>> mappingFullClassName = " + mappingFullClassName);
            System.out.println(TAG + " >>> class content = \n" + builder);
    
    
            // 写入自动生成的类到本地文件中
            try {
                JavaFileObject source = processingEnv.getFiler().createSourceFile(mappingFullClassName);
                Writer writer = source.openWriter();
                writer.write(builder.toString());
                writer.flush();
                writer.close();
            } catch (Exception e) {
                throw new RuntimeException("Error while create file", e);
            }
    
    
            // 写入json到本地文件中
            // 获取 kapt 的参数 root_project_dir
            String rootDir = processingEnv.getOptions().get("root_project_dir");
            
            File rootDirFile = new File(rootDir);
            if (!rootDirFile.exists()) {
                throw new RuntimeException("root_project_dir not exist!");
            }
    
            File routerFileDir = new File(rootDirFile, "router_mapping");
            if (!routerFileDir.exists()) {
                routerFileDir.mkdir();
            }
    
            File mappingFile = new File(routerFileDir, "mapping_" + System.currentTimeMillis() + ".json");
    
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(mappingFile));
                String jsonStr = destinationJsonArray.toString();
                out.write(jsonStr);
                out.flush();
                out.close();
            } catch (Exception e) {
                throw new RuntimeException("Error while writing json", e);
            }
    
    
            System.out.println(TAG + " >>> process finish ...");
    
            return false;
        }
    
        /**
         * 告诉编译器，当前处理器支持的注解类型
         *
         * @return
         */
        @Override
        public Set<String> getSupportedAnnotationTypes() {
            return Collections.singleton(
                    Destination.class.getCanonicalName()
            );
        }
    }
    ```



## 3、应用注解

- settings.gradle 添加

  - ```
    include ':router-annotations'
    include ':router-processor'
    ```

- app module 的 build.gradle 添加

  - ```
    dependencies {
        // 依赖自己的注解
        implementation project(':router-annotations')
        // 依赖自己的注解处理器
        annotationProcessor project(':router-processor')
    }
    ```

- 使用注解

  - ```
    // 使用自己定义的注解
    @Destination(
            url = "router://page-home",
            description = "应用主页"
    )
    public class MainActivity extends Activity {
    }
    ```

- 测试注解

  -     /**
         * 测试注解处理器
         *
         * 异常处理：Mac OS 升级到11.0.1后 ./gradlew :androiddemo:assembleDebug -q 编译项目出错
         * 资源库中找到 Internet Plug-Ins 文件夹，将文件夹名改为 InternetPlug-Ins
         * 参考：https://www.jianshu.com/p/3c1ad32a1def
         *
         * 注意：com.imooc.router.processor.DestinationProcessor 类中的日志，仅在第一次编译时打印.
         *      如果需要再次打印，需要先清楚缓存 ./gradlew clean -q
         *
         * // 1、清除缓存
         * $ ./gradlew clean -q
         *
         * // 2、开始debug编译
         * $ ./gradlew :androiddemo:assembleDebug -q
         *
         * //3、查看生成文件
         * 生成的 RouterMapping_xxx.java 文件在:
         * module 的 build/generated/ap_generated_sources/out/${packagename} 目录下
         */



## 4、发布到本地maven仓库

- rootProject 下的 gradle.properties 添加

  - ```
    POM_URL=../repo
    GROUP_ID=com.imooc.router
    VERSION_NAME=1.0.0
    ```

- router-annotations 项目下新建 gradle.properties 写入

  - ```
    POM_ARTIFACT_ID=router-annotations
    ```

- router-processor 项目下新建 gradle.properties 写入

  - ```
    POM_ARTIFACT_ID=router-processor
    ```

- rootProject 下新建 maven-publish.gradle 写入

  - ```
    // 使用maven插件中的发布功能
    apply plugin: 'maven'
    
    
    // 读取工程配置
    Properties rootProjectProperties = new Properties()
    rootProjectProperties.load(project.rootProject.file('gradle.properties').newDataInputStream())
    def POM_URL = rootProjectProperties.getProperty("POM_URL")
    def GROUP_ID = rootProjectProperties.getProperty("GROUP_ID")
    def VERSION_NAME = rootProjectProperties.getProperty("VERSION_NAME")
    
    Properties childProjectProperties = new Properties()
    childProjectProperties.load(project.file('gradle.properties').newDataInputStream())
    def POM_ARTIFACT_ID = childProjectProperties.getProperty("POM_ARTIFACT_ID")
    
    println("maven-publish POM_URL = $POM_URL")
    println("maven-publish GROUP_ID = $GROUP_ID")
    println("maven-publish VERSION_NAME = $VERSION_NAME")
    println("maven-publish POM_ARTIFACT_ID = $POM_ARTIFACT_ID")
    
    
    // 发布到本地 maven 仓库的任务
    uploadArchives {
        repositories {
            mavenDeployer {
    
                // 填入发布信息
                repository(url: uri(POM_URL)) {
                    pom.groupId = GROUP_ID
                    pom.artifactId = POM_ARTIFACT_ID
                    pom.version = VERSION_NAME
                }
    
                /**
                 * 修改 router-processor 的 build.gradle 内容
                 *
                 * // 原本内容
                 * dependencies { implementation project(':router-annotations') }*
                 * // 修改后的内容
                 * dependencies { implementation 'com.imooc.router:router-annotations:1.0.0' }*/
                pom.whenConfigured { pom ->
                    pom.dependencies.forEach { dep ->
                        if (dep.getVersion() == "unspecified") {
                            dep.setGroupId(GROUP_ID)
                            dep.setVersion(VERSION_NAME)
                        }
                    }
                }
    
            }
        }
    }
    ```

- router-annotations 项目下 build.gradle 应用 maven-publish.gradle 插件

  - ```
    // 应用发布工程
    apply from : rootProject.file("maven-publish.gradle")
    ```

- router-processor 项目下 build.gradle 应用 maven-publish.gradle 插件

  - ```
    // 应用发布工程
    apply from : rootProject.file("maven-publish.gradle")
    ```

- 执行发布命令

  - ```
    /**
     * 开始打包发布：
     *
     * // 清理build文件
     * $ ./gradlew clean -q
     *
     *
     * // 项目上传到maven本地仓库
     * $ ./gradlew :router-annotations:uploadArchives
     * $ ./gradlew :router-processor:uploadArchives
     *
     * */
    ```



## 5、应用maven仓库的aar

- rootProject 的 build.gradle 写入仓库地址

  - ```
    buildscript {
        // 插件所在的仓库
        repositories {
            /**
             * 配置maven仓库地址
             * 这里可以是相对路径地址，也可以是绝对路径地址
             */
            maven {
                url uri("/Users/TaoWang/Documents/Code/github/Android/repo")
            }
    
            google()
            jcenter()
        }
    
        // gradle 插件
        dependencies {
            classpath 'com.android.tools.build:gradle:4.1.3'
    
            /**
             * 声明依赖的插件
             * 形式是：groupId : artifactId : version
             */
            classpath 'com.imooc.router:router-gradle-plugin:1.0.0'
        }
    }
    
    allprojects {
        // 工程依赖所在的仓库
        repositories {
            /**
             * 配置maven仓库地址
             * 这里可以是相对路径地址，也可以是绝对路径地址
             */
            maven {
                url uri("/Users/TaoWang/Documents/Code/github/Android/repo")
            }
    
            google()
            jcenter()
        }
    }
    ```

- app module 的 build.gradle 中引用 注解和注解处理器

  - ```
    dependencies {
        implementation 'com.imooc.router:router-annotations:1.0.0'
        annotationProcessor 'com.imooc.router:router-processor:1.0.0'
    }
    ```

- 验证结果

  - ```
    // 清空build文件
    $ ./gradlew clean -q
    
    // 测试打包
    $ ./gradlew :androiddemo:assembleDebug
    
    // 查看 androiddemo module 的 build/generated/ap_generated_sources/debug/out 目录下生成的代码是否正确
    ```



## 6、kapt的使用

> apt只能收集java的注解，如果还要收集kotlin注解的话，需要使用kapt。

- 根目录的 build.gradle 编写

  - ```
    buildscript {
        dependencies {
            // 添加 kotlin 编译插件
            classpath 'org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.72'
        }
    }
    ```

- app module 的 build.gradle 编写

  - ```
    // kotlin 插件
    apply plugin: 'kotlin-android'
    apply plugin: 'kotlin-kapt'
    
    // 配置 kapt 参数
    android {
        kapt {
            arguments {
                arg("root_project_dir", rootProject.projectDir.absolutePath)
            }
        }
    }
    
    dependencies {
        implementation project(':router-annotations')
        // 搜集 java的注解 和 kotlin的注解
        kapt project(':router-processor')
    }
    ```

- 获取 kapt 的参数 root_project_dir

  - ```
    // 获取 kapt 的参数 root_project_dir
    String rootDir = processingEnv.getOptions().get("root_project_dir");
    
    // 详细代码在 com.imooc.router.processor.DestinationProcesso#process 方法中
    ```