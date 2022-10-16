package com.example.testclassloader;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.lang.reflect.Method;

import com.sun.crypto.provider.AESKeyGenerator;

/**
 * 自定义类加载器
 *
 * @author supengcheng05 <supengcheng05@kuaishou.com>
 * Created on 2022-03-25
 */
public class MyClassLoader extends ClassLoader {
    private String classLoaderName;
    private String path;
    private final String extension = ".class";

    public MyClassLoader(String classLoaderName) {
        super(); //将系统类加载器作为该类加载器的父加载器
        this.classLoaderName = classLoaderName;
    }

    public MyClassLoader(String classLoaderName, ClassLoader parent) {
        super(parent); //显式指定该类加载器的父加载器
        this.classLoaderName = classLoaderName;
    }

    //@Override
    //public String toString() {
    //    return "[" + this.classLoaderName + "]";
    //}

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    protected Class<?> findClass(String className) {
        //确认自定义加载器是否执行
        System.out.println("findClass() className: " + className);
        System.out.println("findClass() classLoaderName: " + toString());

        byte[] data = this.loadClassData(className);
        return this.defineClass(className, data, 0, data.length);
    }

    // 读取文件系统的上二进制字节码
    private byte[] loadClassData(String className) {
        FileInputStream fin = null;
        ByteArrayOutputStream baos = null;
        byte[] data = null;
        // 将全限定类名转换成对应文件系统的层次结构
        className = className.replace(".", "/");
        try {
            // 加上 .class 扩展名
            fin = new FileInputStream(this.path + className + this.extension);
            baos = new ByteArrayOutputStream();
            int ch;
            while (-1 != (ch = fin.read())) {
                baos.write(ch);
            }
            data = baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fin.close();
                baos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    public static void main(String[] args) throws Exception {

        //testClassLoader();

        //testMyClassLoader();

        //testClassUnloading();

        testNameSpace();

        //testNameSpaceV2();

        //testNameSpaceV3();

        //testClassPath();

        //testClassPathV2();
    }

    public static void testClassLoader() {
        ClassLoader appClassLoader = MyClassLoader.class.getClassLoader();
        ClassLoader extClassLoader = appClassLoader.getParent();
        // 启动类加载器由C++代码实现，此处返回null
        ClassLoader bootstrapClassLoader = extClassLoader.getParent();

        System.out.println("应用类加载器: " + appClassLoader);
        System.out.println("扩展类加载器: " + extClassLoader);
        System.out.println("启动类加载器: " + bootstrapClassLoader);
    }

    // 1、自定义类加载器
    public static void testMyClassLoader() throws Exception {
        MyClassLoader myClassLoader = new MyClassLoader("myClassLoader");
        //myClassLoader.setPath("/Users/spc/Documents/project/my_project/test-classloader-demo/target/classes/");
        // 设置你自己的classpath，即 MyClass.class 移动的那个位置，我这边是桌面
        myClassLoader.setPath("/Users/spc/Desktop/");
        Class<?> clazz = myClassLoader.loadClass("com.example.testclassloader.MyClass");
        Object o = clazz.newInstance();
        System.out.println("classloader of instance: " + o.getClass().getClassLoader());
    }

    // 2、类的卸载
    public static void testClassUnloading() throws Exception {
        // 测试时确保classpath下没有 MyClass.class 文件
        // jdk1.8 加上jvm参数 -XX:+TraceClassUnloading 打印类卸载日志

        MyClassLoader myClassLoader1 = new MyClassLoader("myClassLoader1");
        myClassLoader1.setPath("/Users/spc/Desktop/");
        Class<?> clazz1 = myClassLoader1.loadClass("com.example.testclassloader.MyClass");
        System.out.println("hashcode of clazz1: " + clazz1.hashCode());

        // 从 GC Root 中移除引用
        myClassLoader1 = null;
        clazz1 = null;
        System.gc();

        // 也可以通过 jVisualVM 查看类卸载过程
    }

    // 3、类加载器的命名空间
    public static void testNameSpace() throws Exception {
        // 测试前一定要先删除classpath(target/classes/)下的MyClass.class文件
        // 这样的话，自定义类加载器委托系统类加载器去加载的MyClass的时候就加载不到，由自定义加载器自己去path下加载

        MyClassLoader myClassLoader1 = new MyClassLoader("myClassLoader1");
        MyClassLoader myClassLoader2 = new MyClassLoader("myClassLoader2");
        // 如果测试时没删除classpath下的MyClass.class文件，那么下面两个对象的hashcode是一样的，
        // 因为 myClassLoader1、myClassLoader2 都委托给系统类加载器加载了，同一个类加载器只需要加载一次
        myClassLoader1.setPath("/Users/spc/Desktop/");
        myClassLoader2.setPath("/Users/spc/Desktop/");
        Class<?> clazz1 = myClassLoader1.loadClass("com.example.testclassloader.MyClass");
        Class<?> clazz2 = myClassLoader2.loadClass("com.example.testclassloader.MyClass");
        System.out.println("hashcode of clazz1: " + clazz1.hashCode());
        System.out.println("hashcode of clazz2: " + clazz2.hashCode());

        // 也可以这样构造 myClassLoader2: myClassLoader2 = new MyClassLoader("myClassLoader2", myClassLoader1)
        // 指定 myClassLoader1 为 myClassLoader2 的父加载器
        // 这样的话两个Class对象的 hashcode 也是一样的，是同一个对象，因为都是 myClassLoader1 加载的
    }

    // 4、父子类加载器的命名空间的可见性
    public static void testNameSpaceV2() throws Exception {
        // 子加载器加载的类能看到父加载器加载的类
        // 而父加载器加载的类看不到子加载器加载的类

        MyClassLoader myClassLoader = new MyClassLoader("myClassLoader");
        myClassLoader.setPath("/Users/spc/Desktop/");
        Class<?> clazz = myClassLoader.loadClass("com.example.testclassloader.DogHouse");
        Object o = clazz.newInstance();
    }

    // 5、非父子命名空间的之间的可见性
    public static void testNameSpaceV3() throws Exception {
        // 未删除classpath下的Cat.class，没有问题
        // 将classpath下的Cat.class移动到desktop后，报错
        // 异常原因很有意思
        // Caused by: java.lang.ClassCastException: com.example.testclassloader.Cat cannot be cast to com.example.testclassloader.Cat

        MyClassLoader myClassLoader1 = new MyClassLoader("myClassLoader1");
        MyClassLoader myClassLoader2 = new MyClassLoader("myClassLoader2");
        myClassLoader1.setPath("/Users/spc/Desktop/");
        myClassLoader2.setPath("/Users/spc/Desktop/");
        Class<?> clazz1 = myClassLoader1.loadClass("com.example.testclassloader.Cat");
        Class<?> clazz2 = myClassLoader2.loadClass("com.example.testclassloader.Cat");

        System.out.println(clazz1 == clazz2);

        Object o1 = clazz1.newInstance();
        Object o2 = clazz2.newInstance();

        // 反射调用 setCat 方法
        Method method = clazz1.getMethod("setCat", Object.class);
        method.invoke(o1, o2);
    }

    // 6、不同类加载器加载class文件的位置
    public static void testClassPath() {
        // 启动类加载器
        System.out.println(System.getProperty("sun.boot.class.path"));
        // 扩展类加载器
        System.out.println(System.getProperty("java.ext.dirs"));
        // 系统类加载器
        System.out.println(System.getProperty("java.class.path"));
    }

    // 7、指定加载路径
    public static void testClassPathV2() {
        // 使用扩展类加载器加载class，需要从jar包中加载
        // cd target/classes
        // java -Djava.ext.dirs=./ com.example.testclassloader.MyClassLoader

        AESKeyGenerator aesKeyGenerator = new AESKeyGenerator();
        System.out.println(aesKeyGenerator.getClass().getClassLoader());
    }
}
