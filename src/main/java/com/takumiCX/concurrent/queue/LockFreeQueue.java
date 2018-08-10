package com.takumiCX.concurrent.queue;


/**
 * @author: takumiCX
 * @create: 2018-08-10
 *
 * 队列接口,仅包含入队和出队抽象方法
 **/
public interface LockFreeQueue<E>{

    //入队
    boolean enqueue(E e);

    //出队
    E dequeue();
}
