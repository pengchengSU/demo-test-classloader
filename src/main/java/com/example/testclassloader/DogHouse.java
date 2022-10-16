package com.example.testclassloader;

/**
 * 自定义类
 *
 * @author supengcheng05 <supengcheng05@kuaishou.com>
 * Created on 2022-03-25
 */
public class DogHouse {
    public DogHouse() {
        System.out.println("DogHouse is loaded by: " + this.getClass().getClassLoader());
        new Dog();
    }
}
