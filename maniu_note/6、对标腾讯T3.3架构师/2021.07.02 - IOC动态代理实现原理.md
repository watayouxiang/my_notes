# IOC动态代理实现原理

## 1、生成代理类class

```java
public interface HelloInterface {
    void sayHello();
}

public static void main(String[] args) throws  Exception {
    // 生成一个class二进制文件
    byte[] bytes = ProxyGenerator.generateProxyClass("DavidHelloImpl", new Class[]{HelloInterface.class});

    // 输出二进制文件
    File file = new File("输出路径/DavidHelloImpl.class");
    FileOutputStream outputStream = new FileOutputStream(file);
    outputStream.write(bytes);
    outputStream.flush();
    outputStream.close();
}
```

## 2、静态代理demo

```java
public class HelloProxy implements HelloInterface {
    private final HelloInterface helloInterface = new Hello();
    
    @Override
    public void sayHello() {
        System.out.println("Before invoke sayHello");
        helloInterface.sayHello();
        System.out.println("After invoke sayHello");
    }
}

public static void main(String[] args) {
    HelloProxy proxy = new HelloProxy();
    proxy.sayHello();
}
```

## 3、动态代理demo

```java
public static void main(String[] args) {
    // 被代理者
    Hello hello = new Hello();
    // 代理
    HelloInterface proxy = (HelloInterface) Proxy.newProxyInstance(
            hello.getClass().getClassLoader(), 
            hello.getClass().getInterfaces(), 
            new ProxyHandler(hello)// 持有被代理者引用
    );
    proxy.sayHello();
}

// 代理处理器
static class ProxyHandler implements InvocationHandler {
    private final Object object;

    public ProxyHandler(Object object) {
        this.object = object;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("Before invoke " + method.getName());
        Object ob = method.invoke(object, args);
        System.out.println("After invoke " + method.getName());
        return ob;
    }
}

// 控制台输出如下：
// Before invoke sayHello
// sayHello
// After invoke sayHello
```

Proxy.newProxyInstance 方法内部做了三件事：

1. ProxyGenerator.generateProxyClass 方法生成了一个class
2. 加载这个class
3. 实例化对象

## 4、java反射回顾

```java
package com.watayouxiang.myjava.reflect;

public class Person {
    private String name;

    public Person(String name) {
        super();
        this.name = name;
    }

    private void say(String txt) {
        System.out.println(txt);
    }
}
```

```java
// 获取字节码
Class personClazz = Class.forName("com.watayouxiang.android.java.reflect.Person");

// 获取构造函数，并实例化对象
Constructor personConstructor = personClazz.getConstructor(String.class);
Object person = personConstructor.newInstance("wata");

// 获取私有字段 name
Field nameField = personClazz.getDeclaredField("name");
nameField.setAccessible(true);
Object name = nameField.get(person);

// 调用有参私有方法
Method sayMethod = personClazz.getDeclaredMethod("say", String.class);
sayMethod.setAccessible(true);
sayMethod.invoke(person, "新年快乐~");
```

## 5、java注解回顾

- @Retention - 标识这个注解怎么保存，是只在代码中，还是编入class文件中，或者是在运行时可以通过反射访问
- @Target - 标记这个注解应该是哪种 Java 成员

## 6、IOC实现butterknife实战

> IOC 实现：注入布局、注入成员变量、注入"点击事件/长按事件"
>
> 注解+反射+动态代理 实现 butterknife

### 1）使用示例

```java
@InjectLayout(R.layout.activity_main)
public class MainActivity extends BaseActivity {
  
    @InjectView(R.id.text)
    TextView textView;
  
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InjectManager.inject(this);
    }

    @OnClick({R.id.btn, R.id.text})
    public void testClick(View view) {
        Toast.makeText(this, "单击", Toast.LENGTH_SHORT).show();
    }
  
    @OnLongClick({R.id.btn, R.id.text})
    public boolean testLongClick(View v) {
        Toast.makeText(this, "长按", Toast.LENGTH_SHORT).show();
        return true;
    }
}
```

### 2）申明注解

 @OnClick、@OnLongClick、@ViewInject、@ContentView

```java
// --------------------------- InjectEvent
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectEvent {
    //事件三要素
    //1、setOnClickListener
    //2、View.onClickListener
    //3、onClick
    String listenerSetter();
    Class<?> listenerType();
    String callbackMethod();
}

// --------------------------- OnClick

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@InjectEvent(listenerSetter = "setOnClickListener",
        listenerType = View.OnClickListener.class,
        callbackMethod = "onClick")
public @interface OnClick {
    int[] value();
}

// --------------------------- OnLongClick

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@InjectEvent(listenerSetter = "setOnLongClickListener", 
        listenerType = View.OnLongClickListener.class, 
        callbackMethod = "onLongClick")
public @interface OnLongClick {
    int[] value() default -1;
}

// --------------------------- InjectLayout

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectLayout {
    int value();
}

// --------------------------- InjectView

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectView {
    int value();
}
```

### 3）实现注解功能

> 注解+反射+动态代理 实现 butterknife

```java
public class InjectManager {

    public static void inject(Object obj) {
        // 布局注入
        injectLayout(obj);
        // 控件注入
        injectViews(obj);
        // 事件注入
        injectEvent(obj);
    }

    private static void injectEvent(final Object obj) {
        Class<?> clazz = obj.getClass();

        // 获取类中的所有方法
        Method[] methods = clazz.getDeclaredMethods();
        for (final Method method : methods) {

            // 获取方法中的所有注解如：@OnClick, @OnLongClick, @Override...
            Annotation[] annotations = method.getAnnotations();
            for (Annotation annotation : annotations) {

                // 只获取 @InjectEvent 类型的注解
                Class<? extends Annotation> annotationType = annotation.annotationType();
                InjectEvent injectEvent = annotationType.getAnnotation(InjectEvent.class);
                if (injectEvent == null) continue;

                // 获取 @InjectEvent 注解的事件三要素
                String listenerSetter = injectEvent.listenerSetter();// "setOnClickListener"
                Class<?> listenerType = injectEvent.listenerType();// View.onClickListener.class
                String callbackMethod = injectEvent.callbackMethod();// "onClick"

                // --------------------------------------------------------
                try {
                    // 获取 @InjectEvent 注解的 value 方法
                    // 通过调用该方法，从而拿到 viewIds
                    Method valueMethod = annotationType.getDeclaredMethod("value");
                    int[] viewIds = (int[]) valueMethod.invoke(annotation);
                    if (viewIds == null) continue;

                    for (int id : viewIds) {
                        // 通过 View view = findViewById(R.id.my_view) 方法，拿到 view 对象
                        Method findViewById = clazz.getMethod("findViewById", int.class);
                        View view = (View) findViewById.invoke(obj, id);
                        if (view == null) continue;

                        // 拿到 View 的 setOnClickListener(View.onClickListener l) 方法
                        Method setterMethod = view.getClass().getMethod(listenerSetter, listenerType);
                        // 动态代理
                        Object proxy = Proxy.newProxyInstance(
                                // ClassLoader loader
                                listenerType.getClassLoader(),
                                // Class<?>[] interfaces
                                new Class[]{listenerType},
                                // InvocationHandler h
                                new InvocationHandler() {
                                    @Override
                                    public Object invoke(Object _proxy, Method _method, Object[] args) 
                                      		throws Throwable {
                                        return method.invoke(obj, args);
                                    }
                                }
                        );
                        // 调用 View 的 setOnClickListener(View.onClickListener l) 方法
                        setterMethod.invoke(view, proxy);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // --------------------------------------------------------

            }
        }
    }

    private static void injectViews(Object obj) {
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            InjectView injectView = field.getAnnotation(InjectView.class);
            if (injectView != null) {
                try {
                    // TextView textView = findViewById(R.id.text);
                    Method method = clazz.getMethod("findViewById", int.class);
                    int viewId = injectView.value();
                    View view = (View) method.invoke(obj, viewId);
                    field.setAccessible(true);
                    field.set(obj, view);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void injectLayout(Object obj) {
        Class<?> clazz = obj.getClass();
        InjectLayout injectLayout = clazz.getAnnotation(InjectLayout.class);
        if (injectLayout != null) {
            // setContentView(R.layout.activity_main);
            try {
                Method method = clazz.getMethod("setContentView", int.class);
                int value = injectLayout.value();
                method.invoke(obj, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
```

