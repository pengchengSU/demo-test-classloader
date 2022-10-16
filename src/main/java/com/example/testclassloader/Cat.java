package com.example.testclassloader;

/**
 * 自定义类
 *
 * @author supengcheng05 <supengcheng05@kuaishou.com>
 * Created on 2022-03-26
 */
public class Cat {
    private Cat cat;

    public void setCat(Object cat) {
        this.cat = (Cat) cat;
    }
}
