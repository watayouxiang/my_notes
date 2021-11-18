## 一、APT补充

### 1、策略模式

> 策略模式：https://www.runoob.com/design-pattern/strategy-pattern.html

```java
package com.example.spidemo;
public interface SPIService {
    void execute();
}

package com.example.spidemo;
public class SpiImpl1 implements SPIService{
    @Override
    public void execute() {
        System.out.println("SpiImpl1.execute()");
    }
}

package com.example.spidemo;
public class SpiImpl2 implements SPIService{
    @Override
    public void execute() {
        System.out.println("SpiImpl2.execute()");
    }
}

package com.example.spidemo;
public class Context {
    private final SPIService spiService;

    public Content(SPIService spiService) {
        this.spiService = spiService;
    }

    public void executeService(){
        spiService.execute();
    }
}

package com.example.spidemo;
import org.junit.Test;
public class ExampleUnitTest {
    @Test
    public void test1() {
        // 测试策略模式
        Context context = new Context(new SpiImpl2());
        content.executeService();
    }
}
```

### 2、SPI机制分析（ServiceLoader）

SPI机制介绍：

- SPI ，全称为 Service Provider Interface，是一种服务发现机制。它通过在ClassPath路径下的META-INF/services文件夹查找文件，自动加载文件里所定义的类。

SPI实现原理：

- 通过读取文本文件中的类全名，在代码层面反射生成接口的实现类，再执行实现类中的方法。是一种策略模式+IOC注入的综合体

```
// 在`src/main/java`同一级目录下，创建`resources/META-INF/services/com.example.spidemo.SPIService` 文件，在文件里写上SPIService的实现类全限定名。

// com.example.spidemo.SPIService 文件内容如下：
com.example.spidemo.SpiImpl1
com.example.spidemo.SpiImpl2
```

```java
package com.example.spidemo;

import org.junit.Test;

import java.util.Iterator;
import java.util.ServiceLoader;

public class ExampleUnitTest {
    @Test
    public void test2() {
        // SPI机制
        // 对象初始化
        ServiceLoader<SPIService> load = ServiceLoader.load(SPIService.class);
        Iterator<SPIService> iterator = load.iterator();

        // 完成方法注入调用
        while (iterator.hasNext()) {// 获取文本文件的内容，得到需要的实现类的名字
            SPIService spiService = iterator.next();// 反射注入实现类的对象
            spiService.execute();// 执行实现类中的方法
        }
    }
}
```

阅读ServiceLoader源码：为了能查看源码，修改 compileSdkVersion 28，targetSdkVersion 28

### 3、通过javac源码分析APT执行原理

通过阅读源码解决如下问题：

了解 javax.annotation.processing.AbstractProcessor#process 的执行流程：

- process是怎么回调的？
  - SPI机制
- 调用的次数是怎么决定的？
  - 和是否有生成文件有关系，有生成文件则调用一次
- 返回值有什么用？
  - 注解是否往下传递，true表示不传递set

## 二、ASM

### 1、逆波兰表达式

> 参考：https://blog.csdn.net/qq_36144258/article/details/95075064

#### 1.1、逆波兰表达式定义

表达式一般由操作数(Operand)、运算符(Operator)组成，例如算术表达式中，**通常把运算符放在两个操作数的中间，这称为中缀表达式(Infix Expression)，如A+B**。波兰数学家Jan Lukasiewicz提出了另一种数学表示法，它有两种表示形式：**把运算符写在操作数之前，称为波兰表达式(Polish Expression)或前缀表达式(Prefix Expression)，如+AB；把运算符写在操作数之后，称为逆波兰表达式(Reverse Polish Expression)或后缀表达式(Suffix Expression)，如AB+**；其中，逆波兰表达式在编译技术中有着普遍的应用。

#### 1.2、将中缀表达式转换成后缀表达式算法

1、从左至右扫描一中缀表达式。

2、若读取的是操作数，则判断该操作数的类型，并将该操作数存入操作数堆栈

3、若读取的是运算符

- 该运算符为左括号"("，则直接存入运算符堆栈。

- 该运算符为右括号")"，则输出运算符堆栈中的运算符到操作数堆栈，直到遇到左括号为止。

- 该运算符为非括号运算符：

  -  若运算符堆栈栈顶的运算符为括号，则直接存入运算符堆栈。
  - 若比运算符堆栈栈顶的运算符优先级高，则直接存入运算符堆栈。
  - 若比运算符堆栈栈顶的运算符优先级低或相等，则输出栈顶运算符到操作数堆栈，并将当前运算符压入运算符堆栈。

- 当表达式读取完成后运算符堆栈中尚有运算符时，则依序取出运算符到操作数堆栈，直到运算符堆栈为空。

#### 1.3、逆波兰表达式求值算法

1、循环扫描语法单元的项目。

2、如果扫描的项目是操作数，则将其压入操作数堆栈，并扫描下一个项目。

3、如果扫描的项目是一个二元运算符，则对栈的顶上两个操作数执行该运算。

4、如果扫描的项目是一个一元运算符，则对栈的最顶上操作数执行该运算。

5、将运算结果重新压入堆栈。

6、重复步骤2-5，堆栈中即为结果值。

#### 1.4、计算案例

- a+b*(c-d)-e/f  中缀表达式
  - abcd-*+ef/-   后缀表达式
- int z=(x+y)*10 中缀表达式
  - int z xy+10*= 后缀表达式

### 2、java文件转class文件基本规则

- Android Studio 安装 ASM Bytecode Viewer Support Kotlin 插件
- 右键点击要生成的java文件，选择 ASM Bytecode Viewer 生成 ASM 代码
- 主要看 Bytecode 分页，和 ASMMified 分页

```
// ---------------------------------------------------------------------------
// “代码” 如下：
// ---------------------------------------------------------------------------

System.out.println("execute time=" + (endTime - startTime) + "ms");

// ---------------------------------------------------------------------------
// “代码” 转 “逆波兰表达式” 如下：
// ---------------------------------------------------------------------------

System.out
"execute time="
endTime
startTime
-
"ms"
+
+
println

// ---------------------------------------------------------------------------
// “转逆波兰表达式的二进制代码” 如下：
// ---------------------------------------------------------------------------

GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
NEW java/lang/StringBuilder
DUP
INVOKESPECIAL java/lang/StringBuilder.<init> ()V
LDC "execute time="
INVOKEVIRTUAL java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
LLOAD 3
LLOAD 1
LSUB
INVOKEVIRTUAL java/lang/StringBuilder.append (J)Ljava/lang/StringBuilder;
LDC "ms"
INVOKEVIRTUAL java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
INVOKEVIRTUAL java/lang/StringBuilder.toString ()Ljava/lang/String;
INVOKEVIRTUAL java/io/PrintStream.println (Ljava/lang/String;)V
```

### 3、ASM框架完成字节码插桩

> 插桩流程：
>
> 1、class文件读到ClassReader(fis)中
>
> 2、调用 ClassReader.accept() 方法对class信息进行修改
>
> 3、修改后的信息通过 ClassWriter 转成 byte[]，并回写到class文件中

#### 3.1、引入ASM依赖

```
dependencies {
    testImplementation 'org.ow2.asm:asm:7.1'
    testImplementation 'org.ow2.asm:asm-commons:7.1'
}
```

#### 3.2、编码

ASMTest.java


```java
package com.example.jvmandasm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface ASMTest {

}
```

InjectTest.class

```
package com.example.jvmandasm;

public class InjectTest {
    public static int i;

    public InjectTest() {
    }

    @ASMTest
    public static void main(String[] var0) throws InterruptedException {
        Thread.sleep(2000L);
    }

    void method() {
    }
}
```

ASMUnitTest.java

```java
package com.example.jvmandasm;

import org.junit.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class ASMUnitTest {
    @Test
    public void test() {
        try {
            FileInputStream fis = new FileInputStream("/Users/TaoWang/Desktop/JvmAndAsm/app/src/test/java/com/example/jvmandasm/InjectTest.class");
            //获取一个分析器
            ClassReader classReader = new ClassReader(fis);
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            //开始插桩
            classReader.accept(new MyClassVisitor(Opcodes.ASM7, classWriter), ClassReader.EXPAND_FRAMES);
            byte[] bytes = classWriter.toByteArray();
            FileOutputStream fos = new FileOutputStream("/Users/TaoWang/Desktop/JvmAndAsm/app/src/test/java/com/example/jvmandasm/InjectTest_wata.class");
            fos.write(bytes);
            fos.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 用来分析类信息
     */
    static class MyClassVisitor extends ClassVisitor {
        public MyClassVisitor(int api) {
            super(api);
        }

        public MyClassVisitor(int api, ClassVisitor classVisitor) {
            super(api, classVisitor);
        }

        /**
         * 当有一个方法执行了，就执行这个API一次，类中有多个方法，这里就会执行多次
         */
        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
            return new MyMethodVisitor(api, methodVisitor, access, name, descriptor);
        }
    }

    /**
     * 用来分析方法
     */
    static class MyMethodVisitor extends AdviceAdapter {
        int startTime;
        int endTime;
        boolean inject = false;

        protected MyMethodVisitor(int api, MethodVisitor methodVisitor, int access, String name, String descriptor) {
            super(api, methodVisitor, access, name, descriptor);
        }

        /**
         * 每读到一个注解，就执行一次
         */
        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            System.out.println(getName() + "-->>" + descriptor);
            if ("Lcom/example/jvmandasm/ASMTest;".equals(descriptor)) {
                inject = true;
            }
            return super.visitAnnotation(descriptor, visible);
        }

        /**
         * 方法进入的时候调用
         */
        @Override
        protected void onMethodEnter() {
            super.onMethodEnter();
            if (!inject) {
                return;
            }

            // ------------------------------------------------------------------
            // long startTime= System.currentTimeMillis();
            // ------------------------------------------------------------------
            // INVOKESTATIC java/lang/System.currentTimeMillis ()J
            invokeStatic(Type.getType("Ljava/lang/System;"), new Method("currentTimeMillis", "()J"));
            // LSTORE 1
            startTime = newLocal(Type.LONG_TYPE);
            storeLocal(startTime);
        }

        /**
         * 当方法退出的时候调用
         */
        @Override
        protected void onMethodExit(int opcode) {
            super.onMethodExit(opcode);
            if (!inject) {
                return;
            }

            // ------------------------------------------------------------------
            // long endTime= System.currentTimeMillis();
            // ------------------------------------------------------------------
            // INVOKESTATIC java/lang/System.currentTimeMillis ()J
            invokeStatic(Type.getType("Ljava/lang/System;"), new Method("currentTimeMillis", "()J"));
            // LSTORE 3
            endTime = newLocal(Type.LONG_TYPE);
            storeLocal(endTime);

            // ------------------------------------------------------------------
            // System.out.println("execute time="+(endTime-startTime)+"ms");
            // ------------------------------------------------------------------
            // GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
            getStatic(Type.getType("Ljava/lang/System;"), "out", Type.getType("Ljava/io/PrintStream;"));
            // NEW java/lang/StringBuilder
            newInstance(Type.getType("Ljava/lang/StringBuilder;"));
            // DUP
            dup();
            // INVOKESPECIAL java/lang/StringBuilder.<init> ()V
            invokeConstructor(Type.getType("Ljava/lang/StringBuilder;"), new Method("<init>", "()V"));
            // LDC "execute time="
            visitLdcInsn("execute time=");
            // INVOKEVIRTUAL java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
            invokeVirtual(Type.getType("Ljava/lang/StringBuilder;"), new Method("append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
            // LLOAD 3
            // LLOAD 1
            loadLocal(endTime);
            loadLocal(startTime);
            // LSUB
            math(SUB, Type.LONG_TYPE);
            // INVOKEVIRTUAL java/lang/StringBuilder.append (J)Ljava/lang/StringBuilder;
            // LDC "ms"
            // INVOKEVIRTUAL java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
            // INVOKEVIRTUAL java/lang/StringBuilder.toString ()Ljava/lang/String;
            // INVOKEVIRTUAL java/io/ PrintStream.println (Ljava/lang/String;)V
            invokeVirtual(Type.getType("Ljava/lang/StringBuilder;"), new Method("append", "(J)Ljava/lang/StringBuilder;"));
            visitLdcInsn(" ms");
            invokeVirtual(Type.getType("Ljava/lang/StringBuilder;"), new Method("append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
            invokeVirtual(Type.getType("Ljava/lang/StringBuilder;"), new Method("toString", "()Ljava/lang/String;"));
            invokeVirtual(Type.getType("Ljava/io/PrintStream;"), new Method("println", "(Ljava/lang/String;)V;"));
        }
    }

}
```

执行后，会输出如下文件：InjectTest_wata.class

```
package com.example.jvmandasm;

public class InjectTest {
    public static int i;

    public InjectTest() {
    }

    @ASMTest
    public static void main(String[] var0) throws InterruptedException {
        long var1 = System.currentTimeMillis();
        Thread.sleep(2000L);
        long var3 = System.currentTimeMillis();
        System.out.println("execute time=" + (var3 - var1) + " ms");
    }

    void method() {
    }
}
```

