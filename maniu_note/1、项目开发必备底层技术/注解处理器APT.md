[TOC]

# 注解处理器APT

## APT简介

### APT是什么？

APT 即为 Annotation Processing Tool，它是 javac 的一个工具，中文意思为编译时注解处理器。

### APT的内部原理？

APT 可以用来在编译时扫描和处理注解。通过 APT 可以获取到注解和被注解对象的相关信息，在拿到这些信息后我们可以根据需求来自动的生成一些代码，省去了手动编代。

### 为什么APT比反射效率高？

获取注解及生成代码都是在代码编译时候完成的，相比反射在运行时处理注解大大提高了程序性能。

### APT的核心类是什么？

APT 的核心是AbstractProcessor类。

### APT的应用有哪些？

APT技术被广泛的运用在Java框架中，包括Android项以及Java后台项目，除了上面我们提到的ButterKnife之外，像EventBus 、Dagger2以及阿里的ARouter路由框架等都运用到APT技术，因此要想了解以、探究这些第三方框架的实现原理，APT就是我们必须要掌握的。

### 安卓中如何注册APT？

由于处理器是javac的工具，因此我们必须将我们自己的处理器注册到javac中。在以前我们需要提供一个.jar文件，打包你的注解处理器到此文件中，并在在你的jar中，需要打包一个特定的文件 `javax.annotation.processing.Processor到META-INF/services路径下` 把MyProcessor.jar放到你的builpath中，javac会自动检查和读取javax.annotation.processing.Processor中的内容，并且注册MyProcessor作为注解处理器。

超级麻烦有木有，不过不要慌，谷歌baba给我们开发了AutoService注解，你只需要引入这个依赖，然后在你的解释器第一行加上

```
@AutoService(Processor.class)
```

然后就可以自动生成META-INF/services/javax.annotation.processing.Processor文件的，省去了打jar包这些繁琐的步骤。

## 注解Annotation

### 复习注解

注解，单独是没有意义的。如下是具体应用场景：

- 注解+APT：用于生成一些java文件，如：butterknife、dagger2、hilt、databinding
- 注解+代码埋点：AspactJ、ARouter
- 注解+反射+动态代理：XUtils、Lifecycle

```java
@Target({ElementType.FIELD, ElementType.METHOD})// 作用在什么地方
@Retention(RetentionPolicy.RUNTIME)							// 作用范围
public @interface BindView {
    String value();
    int id();
}

public enum ElementType {
    TYPE,               /* 类、接口（包括注释类型）或枚举声明 */
    FIELD,              /* 字段声明（包括枚举常量）*/
    METHOD,             /* 方法声明 */
    PARAMETER,          /* 参数声明 */
    CONSTRUCTOR,        /* 构造方法声明 */
    LOCAL_VARIABLE,     /* 局部变量声明 */
    ANNOTATION_TYPE,    /* 注释类型声明 */
    PACKAGE             /* 包声明 */
}

public enum RetentionPolicy {
    SOURCE,            /* Annotation信息仅存在于编译器处理期间，编译器处理完之后就没有该Annotation信息了 */
    CLASS,             /* 编译器将Annotation存储于类对应的.class文件中。默认行为 */
    RUNTIME            /* 编译器将Annotation存储于class文件中，并且可由JVM读入 */
}
```

### @IntDef代替枚举

```java
class Test {
    private static final int SUNDAY = 0;
    private static final int MONDAY = 1;

    // 注解代替枚举，可以节省内存空间
    @IntDef({SUNDAY, MONDAY})
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.SOURCE)
    @interface WeekDay{
    }

    public static void setCurrDay(@WeekDay int currDay) {
    }

    public static void main(String[] args) {
        setCurrDay(SUNDAY);
    }
}
```

## APT的常用api介绍

### AbstractProcessor

```java
public class MyProcessor extends AbstractProcessor {
    // 每一个注解处理器类都必须有一个空的构造函数。
    // 然而，这里有一个特殊的init()方法，它会被注解处理工具调用，并输入ProcessingEnviroment参数。
    // ProcessingEnviroment提供很多有用的工具类Elements, Types和Filer
    @Override
    public synchronized void init(ProcessingEnvironment env){ }
 
    // 这相当于每个处理器的主函数main()。
    // 你在这里写你的扫描、评估和处理注解的代码，以及生成Java文件。
    // 输入参数RoundEnviroment，可以让你查询出包含特定注解的被注解元素。
    // 返回值是一个布尔值，表明注解是否已经被处理器处理完成，官方原文`whether or not the set of annotations are claimed by this processor`，通常在处理出现异常直接返回false、处理完成返回true。
    @Override
    public boolean process(Set<? extends TypeElement> annoations, RoundEnvironment env) { }
 
    // 必须要实现，用来表示这个注解处理器是注册给哪个注解的。
    // 返回值是一个字符串的集合，包含本处理器想要处理的注解类型的合法全称。
    @Override
    public Set<String> getSupportedAnnotationTypes() { }
 
    // 用来指定你使用的Java版本。
    // 通常这里返回SourceVersion.latestSupported()，你也可以使用SourceVersion_RELEASE_6、7、8
    @Override
    public SourceVersion getSupportedSourceVersion() { }
}
```

### Element含义

```java
/** 
 * 表示一个程序元素，比如包、类或者方法，有如下几种子接口： 
 *
 * ExecutableElement：表示某个类或接口的方法、构造方法或初始化程序（静态或实例），包括注解类型元素； 
 * PackageElement：表示一个包程序元素； 
 * TypeElement：表示一个类或接口程序元素； 
 * TypeParameterElement：表示一般类、接口、方法或构造方法元素的形式类型参数； 
 * VariableElement：表示一个字段、enum常量、方法或构造方法参数、局部变量或异常参数 
 */  
public interface Element extends AnnotatedConstruct {  
    /** 
     * 返回此元素定义的类型 
     * 例如，对于一般类元素 C<N extends Number>，返回参数化类型 C<N> 
     */  
    TypeMirror asType();  
  
    /** 
     * 返回此元素的种类：包、类、接口、方法、字段...,如下枚举值 
     * PACKAGE, ENUM, CLASS, ANNOTATION_TYPE, INTERFACE, ENUM_CONSTANT, FIELD, PARAMETER, LOCAL_VARIABLE, EXCEPTION_PARAMETER, 
     * METHOD, CONSTRUCTOR, STATIC_INIT, INSTANCE_INIT, TYPE_PARAMETER, OTHER, RESOURCE_VARIABLE; 
     */  
    ElementKind getKind();  
  
    /** 
     * 返回此元素的修饰符,如下枚举值 
     * PUBLIC, PROTECTED, PRIVATE, ABSTRACT, DEFAULT, STATIC, FINAL, 
     * TRANSIENT, VOLATILE, SYNCHRONIZED, NATIVE, STRICTFP; 
     */  
    Set<Modifier> getModifiers();  
  
    /** 
     * 返回此元素的简单名称,例如 
     * 类型元素 java.util.Set<E> 的简单名称是 "Set"； 
     * 如果此元素表示一个未指定的包，则返回一个空名称； 
     * 如果它表示一个构造方法，则返回名称 "<init>"； 
     * 如果它表示一个静态初始化程序，则返回名称 "<clinit>"； 
     * 如果它表示一个匿名类或者实例初始化程序，则返回一个空名称 
     */  
    Name getSimpleName();  
  
    /** 
     * 返回封装此元素的最里层元素。 
     * 如果此元素的声明在词法上直接封装在另一个元素的声明中，则返回那个封装元素； 
     * 如果此元素是顶层类型，则返回它的包； 
     * 如果此元素是一个包，则返回 null； 
     * 如果此元素是一个泛型参数，则返回 null. 
     */  
    Element getEnclosingElement();  
  
    /** 
     * 返回此元素直接封装的子元素 
     */  
    List<? extends Element> getEnclosedElements();  
    
    boolean equals(Object var1);
 
    int hashCode();
  
    /** 
     * 返回直接存在于此元素上的注解 
     * 要获得继承的注解，可使用 getAllAnnotationMirrors 
     */  
    List<? extends AnnotationMirror> getAnnotationMirrors();  
  
    /** 
     * 返回此元素针对指定类型的注解（如果存在这样的注解），否则返回 null。注解可以是继承的，也可以是直接存在于此元素上的 
     */   
    <A extends Annotation> A getAnnotation(Class<A> annotationType); 
     
    //接受访问者的访问 （？？）
     <R, P> R accept(ElementVisitor<R, P> var1, P var2);
}  

```

### 辅助接口

在自定义注解器的初始化时候，可以获取以下4个辅助接口：

```java
 public class MyProcessor extends AbstractProcessor {  
      
        private Types typeUtils;  
        private Elements elementUtils;  
        private Filer filer;  
        private Messager messager;  
      
        @Override  
        public synchronized void init(ProcessingEnvironment processingEnv) {  
            super.init(processingEnv);  
            typeUtils = processingEnv.getTypeUtils();  
            elementUtils = processingEnv.getElementUtils();  
            filer = processingEnv.getFiler();  
            messager = processingEnv.getMessager();  
        }  
    }  

```

**Filer：**一般配合JavaPoet来生成需要的java文件（下一篇将详细介绍javaPoet）

**Messager：**因为在process()中不能抛出一个异常，那会使运行注解处理器的JVM崩溃。所以Messager提供给注解处理器一个报告错误、警告以及提示信息的途径，用来写一些信息给使用此注解器的第三方开发者看。

**Types：**Types是一个用来处理TypeMirror的工具

**Elements：**Elements是一个用来处理Element的工具

## APT实现butterknife

利用APT实现，在 application 工程的 `/build/generated/ap_generated_sources/debug/out/...` 路径下生成如下代码：

```java
package com.watayouxiang.aptdemo;
import com.watayouxiang.apt_annotation.IBinder;

public class MainActivity_ViewBinding implements IBinder<com.watayouxiang.aptdemo.MainActivity>{
	@Override
	public void bind(com.watayouxiang.aptdemo.MainActivity target){
		target.textView=(android.widget.TextView)target.findViewById(2131231118);
	}
}
```

开发所用的 gradle 版本：

```groovy
// gradle 版本
distributionUrl=https\://services.gradle.org/distributions/gradle-7.0.2-bin.zip
// gradle 插件版本
classpath "com.android.tools.build:gradle:7.0.3"
```

### 1）新建一个 java-library 类型的注解工程 "apt_annotation"

```java
package com.watayouxiang.apt_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface BindView {
    int value();
}
```

```java
package com.watayouxiang.apt_annotation;

public interface IBinder<T> {
    void bind(T target);
}
```

```java
package com.watayouxiang.apt_annotation;

public class MyButterKnife {
    // 相当于：new MainActivity_ViewBinding().bind(this);
    public static void bind(Object activity) {
        String name = activity.getClass().getName() + "_ViewBinding";
        try {
            Class<?> aClass = Class.forName(name);
            IBinder iBinder = (IBinder) aClass.newInstance();
            iBinder.bind(activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### 2）新建一个 java-ibrary 类型的注解处理器工程 "apt_compiler"

```java
package com.watayouxiang.apt_annotation_compiler;

import com.google.auto.service.AutoService;
import com.watayouxiang.apt_annotation.BindView;
import com.watayouxiang.apt_annotation.IBinder;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * APT: Annotation Processing Tool
 */
@AutoService(Processor.class)
public class AnnotationsCompiler extends AbstractProcessor {

    // 定义一个只能用来生成 APT 目录下面的文件的对象
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
    }

    // 支持的版本
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return super.getSupportedSourceVersion();
    }

    // 能用来处理哪些注解
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        // 只能处理 @Deprecated 类型的注解
        Set<String> types = new HashSet<>();
        types.add(BindView.class.getCanonicalName());
        return types;
    }

    /**
     * 目的是在 application 工程的 `/build/generated/ap_generated_sources/debug/out/...` 路径下生成如下代码：
     *
     * <blockquote><pre>
     * package com.example.dn_butterknife;
     * import com.example.dn_butterknife.IBinder;
     * public class MainActivity_ViewBinding implements IBinder<com.example.dn_butterknife.MainActivity> {
     *     @Override
     *     public void bind(com.example.dn_butterknife.MainActivity target) {
     *         target.textView = (android.widget.TextView) target.findViewById(2131165359);
     *     }
     * }
     * </pre></blockquote>
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 1.process是怎么回调的？     SPI机制
        // 2.调用的次数是怎么决定的？    和是否有生成文件有关系
        // 3.返回值有什么用？          注解是否往下传递，true表示不传递set
        if (annotations.isEmpty()) {
            return false;
        }

        // 日志打印
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "wataTAG: " + annotations);

        // 1、获取APP中所有用到了BindView注解的对象
        Set<? extends Element> elementsAnnotatedWith = roundEnv.getElementsAnnotatedWith(BindView.class);

        // 2、开始对elementsAnnotatedWith进行分类
        //
        // Element 的子类有如下：
        //
        // TypeElement //类
        // ExecutableElement //方法
        // VariableElement //属性
        //
        HashMap<String, List<VariableElement>> map = new HashMap<>();
        for (Element element : elementsAnnotatedWith) {
            // @BindView 是属性类型，所以直接强转
            VariableElement variableElement = (VariableElement) element;
            // 获取 @BindView 所在的作用域，也就是Activity类，拿到Activity的名称
            String activityName = variableElement.getEnclosingElement().getSimpleName().toString();
            // 获取 @BindView 所在的作用域，也就是Activity类，拿到Activity的字节码对象
            Class<? extends Element> activityClass = variableElement.getEnclosingElement().getClass();
            // [
            //   "MainActivity" : {VariableElement1, VariableElement2, ...}
            //   "TwoActivity" : {VariableElement1, VariableElement2}
            //   "ThreeActivity" : {VariableElement1}
            // ]
            List<VariableElement> variableElements = map.get(activityName);
            if (variableElements == null) {
                variableElements = new ArrayList<>();
                map.put(activityName, variableElements);
            }
            variableElements.add(variableElement);
        }

        // 3、开始生成文件
        //
        // package com.watayouxiang.aptdemo;
        // import com.watayouxiang.apt_annotation.IBinder;
        //
        // public class MainActivity_ViewBinding implements IBinder<MainActivity> {
        //     @Override
        //     public void bind(com.watayouxiang.aptdemo.MainActivity target) {
        //         target.textView = (android.widget.TextView) target.findViewById(2131231118);
        //     }
        // }
        //
        if (map.size() > 0) {
            Writer writer = null;
            for (String activityName : map.keySet()) {
                // 拿到某个 Activity 中的所有注解
                List<VariableElement> variableElements = map.get(activityName);
                // 拿到 Activity 包名
                Element enclosingElement = variableElements.get(0).getEnclosingElement();
                String packageName = processingEnv.getElementUtils().getPackageOf(enclosingElement).toString();
                try {
                    // 开始生成 MainActivity_ViewBinding.java 文件
                    // 创建名为 com.watayouxiang.aptdemo.MainActivity 的.java文件
                    JavaFileObject sourceFile = filer.createSourceFile(packageName + "." + activityName + "_ViewBinding");

                    writer = sourceFile.openWriter();
                    // package com.watayouxiang.aptdemo;
                    writer.write("package " + packageName + ";\n");
                    // import com.watayouxiang.apt_annotation.IBinder;
                    writer.write("import " + IBinder.class.getPackage().getName() + ".IBinder;\n\n");
                    // public class MainActivity_ViewBinding implements IBinder<com.watayouxiang.aptdemo.MainActivity>{
                    writer.write("public class " + activityName + "_ViewBinding implements IBinder<" + packageName + "." + activityName + ">{\n");
                    // @Override
                    // public void bind(com.watayouxiang.aptdemo.MainActivity target) {
                    writer.write("\t@Override\n");
                    writer.write("\tpublic void bind(" + packageName + "." + activityName + " target){\n");
                    // target.tvText=(android.widget.TextView)target.findViewById(2131165325);
                    for (VariableElement variableElement : variableElements) {
                        // 得到名字
                        String variableName = variableElement.getSimpleName().toString();
                        // 得到 ID
                        int id = variableElement.getAnnotation(BindView.class).value();
                        // 得到类型
                        TypeMirror typeMirror = variableElement.asType();
                        writer.write("\t\ttarget." + variableName + "=(" + typeMirror + ")target.findViewById(" + id + ");\n");
                    }
                    // }}
                    writer.write("\t}\n");
                    writer.write("}");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        return false;
    }
}
```

### 3）新建一个 application 类型工程 "app"

```java
package com.watayouxiang.aptdemo;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import com.watayouxiang.apt_annotation.BindView;

// 使用自定义的 APT
public class MainActivity extends AppCompatActivity {
  
    @BindView(R.id.tv_text)
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyButterKnife.bind(this);

        textView.setText("APT 实现 ButterKnife");
    }
}
```

```groovy
// app gradle 文件中添加如下依赖：
implementation project(path: ':annotations')
annotationProcessor project(path: ':annotation_compiler')
```

