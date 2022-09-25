package com.HzeLng.demo;

/**
 * @author HzeLng
 * @version 1.0
 * @description B
 * @date 2022/3/30 23:09
 */
public class B {
    public void methodB(){
        System.out.println("methodB");
        if(true){
            throw new RuntimeException();
        }
    }
}
