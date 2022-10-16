package com.example.testclassloader;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ServiceLoader;

/**
 * 线程上下文加载器测试类
 *
 * @author supengcheng05 <supengcheng05@kuaishou.com>
 * Created on 2022-03-25
 */
public class TestThreadContextClassLoader {
    public static void main(String[] args) throws Exception {

        //testContextClassLoader();

        //testJDBC();

        testJDBCV2();
    }

    // 初探线程上下文加载器
    public static void testContextClassLoader() {
        /*
        当前类加载器 (Current class loader)
        每个类都会使用自己的类加载器（即加载自身的类加载器）来去加载其他类（指的是所依赖的类），
        如果classX引用了classY，那么classX的类加载器就会去加载c1assY（前提是class立尚余被加载）

        线程上下文类加载器 (Context classloader)
        线程上下文类加载器是从JDK 1.2开始引入的，类Thread中的getContextClassLoader()与setContextClassLoader
        如果没有通过setContextClassLoader进行设置的话，线程将继承其父线程的上下文类加载器。
        Java应用运行时的初始线程的上下文类加载器是系统类加载器，在线程中运行的代码可以通过该类加载器来加载类与资源。
         */
        System.out.println(Thread.currentThread().getContextClassLoader());
        System.out.println(Thread.class.getClassLoader());
    }

    // SPI 之 JDBC
    public static void testJDBC() {
        // /META-INF/services

        ServiceLoader<Driver> loaders = ServiceLoader.load(Driver.class);
        for (Driver driver : loaders) {
            System.out.println("driver: " + driver.getClass() + ", loader: " + driver.getClass().getClassLoader());
        }
        System.out.println("当前线程上下文类加载器: " + Thread.currentThread().getContextClassLoader());
        System.out.println("ServiceLoader的类加载器: " + ServiceLoader.class.getClassLoader());
    }

    // SPI 之 JDBC
    public static void testJDBCV2() throws Exception {
        // 会使 com.mysql.jdbc.Driver 初始化，执行 static 代码块
        // DriverManager 初始化，执行 static 代码块

        Class.forName("com.mysql.jdbc.Driver");
        System.out.println("classloader of com.mysql.jdbc.Driver: " + com.mysql.jdbc.Driver.class.getClassLoader());
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/mytestdb", "username", "password");
    }
}
