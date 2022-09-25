package com.HzeLng.demo;

import com.practise.server.MainApplication;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author HzeLng
 * @version 1.0
 * @description A
 * @date 2022/3/30 23:09
 */
@RunWith(SpringRunner.class)
// 要加这个classes，不然 no beans of RpcProxy 应该是容器没开启，没有识别到
@SpringBootTest(classes = MainApplication.class)
public class A {
    B b;
    int n1;
    int n2;
    public A(B b){
        this.b = b;
        n1 = 1;
        n2 = 1;
    }
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    public void methodA(){
        System.out.println("methodA");
        n1++;
        n2++;
        System.out.println("before");
        b.methodB();
        System.out.println("after");
    }
    public void methodAPrint(){
        System.out.println(n1);
        System.out.println(n2);
    }

    @Test
    public void test() {
        B b = new B();
        A a = new A(b);
        try{
            a.methodA();
        }catch (Exception e){

        }
        a.methodAPrint();
    }
}
