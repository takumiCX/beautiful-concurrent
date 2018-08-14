package com.takumiCX.concurrent.queue;


import org.junit.Test;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author: takumiCX
 * @create: 2018-08-10
 **/
public class QueueTest {

    static int testCount=100;

    static int threadNum=200;

    static int queueOpNum=10000;


    @Test
    public void test() throws InterruptedException {

        testConcurrentLinkedQueue();

        testLockFreeLinkedQueue();

        testLinkedBlockingQueue();

        testSingleLinkedQueue();

    }

    private void testSingleLinkedQueue() throws InterruptedException {

        long sum = 0L;
        for (int i = 0; i < testCount; i++) {
            sum += singleLinkedQueue();
        }
        System.out.println("LockFreeSingleLinkedQueue: " + sum * 1.0 / testCount);


    }

    private long singleLinkedQueue() throws InterruptedException {

        final LockFreeQueue<String> queue = new LockFreeSingleLinkedQueue<>();

        final CountDownLatch latch = new CountDownLatch(threadNum);

        long start = System.currentTimeMillis();

        for (int i = 0; i < threadNum; i++) {
            final int finalI = i;
            new Thread(new Runnable() {
                public void run() {
                    for (int j = 0; j < queueOpNum; j++) {
                        if (j%2==0) {
                            queue.enqueue(finalI + "-" + j);
                        } else {
                            queue.dequeue();
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

    private void testLockFreeLinkedQueue() throws InterruptedException {
        long sum = 0L;
        for (int i = 0; i < testCount; i++) {
            sum += testQueue(new QueueAdapter<String>(new LockFreeLinkedQueue<String>()));
        }
        System.out.println("LockFreeLinkedQueue: " + sum * 1.0 / testCount);

    }

//    private long lockFree() throws InterruptedException {
//
//        final LockFreeQueue queue = new LockFreeLinkedQueue<String>();
//
//        final CountDownLatch latch = new CountDownLatch(10);
//
//        long start = System.currentTimeMillis();
//
//        for (int i = 0; i < 10; i++) {
//            final int finalI = i;
//            new Thread(new Runnable() {
//                public void run() {
//                    for (int j = 0; j < 10000; j++) {
//                        if (j % 2 == 0) {
//                            queue.enqueue(finalI + "-" + j);
//                        } else {
//                            queue.dequeue();
//                        }
//                    }
//                    latch.countDown();
//                }
//            }).start();
//        }
//        latch.await();
//        long end = System.currentTimeMillis();
//        return (end - start);
//    }


    private void testConcurrentLinkedQueue() throws InterruptedException {

        long sum = 0L;
        for (int i = 0; i < testCount; i++) {
            sum += testQueue(new ConcurrentLinkedQueue<String>());
        }
        System.out.println("ConcurrentLinkedQueue: " + sum * 1.0 / testCount);

    }

    private void testLinkedBlockingQueue() throws InterruptedException {

        long sum = 0L;
        for (int i = 0; i < testCount; i++) {
            sum += testQueue(new LinkedBlockingQueue<String>());
        }
        System.out.println("LinkedBlockingQueue: " + sum * 1.0 / testCount);

    }

    private long testQueue(Queue<String> qUeue) throws InterruptedException {

        final Queue queue = qUeue;

        final CountDownLatch latch = new CountDownLatch(threadNum);

        long start = System.currentTimeMillis();

        for (int i = 0; i < threadNum; i++) {
            final int finalI = i;
            new Thread(new Runnable() {
                public void run() {
                    for (int j = 0; j < queueOpNum; j++) {
                        if (j%2==0) {
                            queue.offer(finalI + "-" + j);
                        } else {
                            queue.poll();
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


    /**
     * 适配器
     * @param <E>
     */
    private static class QueueAdapter<E> extends AbstractQueue<E> {

        public LockFreeQueue<E> queue;

        public QueueAdapter(LockFreeQueue<E> queue) {
            this.queue = queue;
        }

        @Override
        public Iterator<E> iterator() {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean offer(E e) {
            return queue.enqueue(e);
        }

        @Override
        public E poll() {
            return queue.dequeue();
        }

        @Override
        public E peek() {
            return null;
        }
    }
}
