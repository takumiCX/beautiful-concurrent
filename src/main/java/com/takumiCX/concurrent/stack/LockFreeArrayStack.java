package com.takumiCX.concurrent.stack;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: takumiCX
 * @create: 2018-08-08
 *
 * 基于数组实现的无锁的并发栈
 **/
public class LockFreeArrayStack<E> implements LockFreeStack<E>{

    //不支持扩容
    final Object[] elements;

    //容量,一旦确定不可更改
    final int capacity;

    //记录栈顶元素在数组中的下标,初始值为-1
    AtomicInteger top = new AtomicInteger(-1);

    public LockFreeArrayStack(int capacity) {
        this.capacity = capacity;
        elements = new Object[capacity];
    }


    /**
     * 入栈
     *
     * @param e
     * @return true:入栈成功 false:入栈失败(栈已满)
     */
    public boolean push(E e) {
        //死循环
        for (; ; ) {
            //当前栈顶元素在数组中的下标
            int curTop = top.get();
            //栈已满,返回false
            if (curTop + 1 >= capacity) {
                return false;
            } else {
                //首先将元素放入栈中
                elements[curTop + 1] = e;
                //基于CAS更新栈顶指针,这里是top值
                if (top.compareAndSet(curTop, curTop + 1)) {
                    return true;
                }
            }

        }
    }

    /**
     * 出栈
     *
     * @return 栈顶元素, 若栈为空返回null
     */
    public E pop() {
        //死循环
        for (; ; ) {
            //当前栈顶元素在数组中的下标
            int curTop = top.get();
            //栈为空,返回null
            if (curTop == -1) {
                return null;
            } else {
                //CAS更新栈顶指针,这里是top值
                if (top.compareAndSet(curTop, curTop - 1)) {
                    return (E) elements[curTop];
                }
            }

        }
    }
}
