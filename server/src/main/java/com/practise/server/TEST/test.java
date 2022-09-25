package com.practise.server.TEST;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author HzeLng
 * @version 1.0
 * @description test
 * @date 2022/3/30 22:53
 */
public class test {
    int a;
    int b;
    int[] c;
    public test(){
        a = 1;
        b = 1;
        c = new int[3];
    }
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    public void A(){
        a++;
        b++;
        System.out.println("before B");
        if(true){
            throw new RuntimeException();
        }
        B();
        System.out.println("after B");
        a++;
        b++;


    }

    public void B() {
        System.out.println("before exception");
        a++;
        b++;
        if(true){
            throw new RuntimeException();
        }
        System.out.println("after");

    }
    public void C(){
        System.out.println(a);
        System.out.println(b);
    }

    public static void main(String[] args) {
        test t = new test();
        try{
            t.A();
        }catch (Exception e){

        }
        t.C();
    }

}
