package com.takumiCX.concurrent.stack;

/**
 * @author: takumiCX
 * @create: 2018-08-09
 **/
public interface LockFreeStack<E> {

    boolean push(E e);

    E pop();
}
