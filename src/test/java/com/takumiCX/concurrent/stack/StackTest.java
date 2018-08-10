package com.takumiCX.concurrent.stack;

import org.junit.Test;

import java.util.Stack;
import java.util.concurrent.CountDownLatch;

/**
 * @author: takumiCX
 * @create: 2018-08-09
 **/
public class StackTest {


    /**
     * 测试LockFreeArrayStack,LockFreeLinkedStack,以及JDK中的Stack在并发环境中的入栈出栈性能:
     * 分别开启10个线程,每个线程混合入栈出栈10000次,通过重复100次上述过程计算出平均值并打印。
     * @throws InterruptedException
     */
    @Test
    public void test() throws InterruptedException {

        testArrayStack();
        testJDKStack();
        testLinkedStack();
    }

    private static void testLinkedStack() throws InterruptedException {

        long sum = 0L;
        for (int i = 0; i < 100; i++) {
            sum += lockFreeStack(new LockFreeLinkedStack());
        }
        System.out.println("LockFreeLinkedStack: " + sum * 1.0 / 100);

    }

    private static void testArrayStack() throws InterruptedException {

        long sum = 0L;
        for (int i = 0; i < 100; i++) {
            sum += lockFreeStack(new LockFreeArrayStack(100000));
        }
        System.out.println("LockFreeArrayStack: " + sum * 1.0 / 100);

    }

    private static long lockFreeStack(LockFreeStack lockFreeStack) throws InterruptedException {

        final LockFreeStack stack = lockFreeStack;

        final CountDownLatch latch = new CountDownLatch(10);

        long start = System.currentTimeMillis();

        for (int i = 0; i < 10; i++) {
            final int finalI = i;
            new Thread(new Runnable() {
                public void run() {
                    for (int j = 0; j < 10000; j++) {
                        if (j % 2 == 0) {
                            stack.push(finalI + "-" + j);
                        } else {
                            stack.pop();
                        }
                    }
                    latch.countDown();
                }
            }).start();
        }
        latch.await();
        long end = System.currentTimeMillis();
        return (end - start);
    }

    private static void testJDKStack() throws InterruptedException {

        long sum = 0L;
        for (int i = 0; i < 100; i++) {
            sum += jdkStack();
        }
        System.out.println("JDKStack: " + sum * 1.0 / 100);

    }

    private static long jdkStack() throws InterruptedException {
        final Stack<String> stack = new Stack<String>();

        final CountDownLatch latch = new CountDownLatch(10);

        final Object lock = new Object();

        long start = System.currentTimeMillis();

        for (int i = 0; i < 10; i++) {

            final int finalI = i;
            new Thread(new Runnable() {
                public void run() {
                    for (int j = 0; j < 10000; j++) {
                        synchronized (lock) {
                            if (j % 2 == 0) {
                                stack.push(finalI + "-" + j);
                            } else {

                                stack.pop();
                            }
                        }
                    }

                    latch.countDown();
                }
            }).start();

        }

        latch.await();

        long end = System.currentTimeMillis();

        return (end - start);
    }


}
