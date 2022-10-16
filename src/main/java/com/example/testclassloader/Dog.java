package com.example.testclassloader;

/**
 * 自定义类
 *
 * @author supengcheng05 <supengcheng05@kuaishou.com>
 * Created on 2022-03-25
 */
public class Dog {
    public Dog() {
        System.out.println("Dog is loaded by: " + this.getClass().getClassLoader());

        //System.out.println("from Dog: " + DogHouse.class);
    }
}
