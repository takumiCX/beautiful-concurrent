package com.takumiCX.concurrent.stack;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author: takumiCX
 * @create: 2018-08-09
 *
 * 基于链表实现的无锁的并发栈
 **/
public class LockFreeLinkedStack<E> implements LockFreeStack<E>{

    //栈顶指针
    AtomicReference<Node<E>> top = new AtomicReference<Node<E>>();

    /**
     * @param e 入栈元素
     * @return true:入栈成功 false:入栈失败
     */
    public boolean push(E e) {

        //构造新结点
        Node<E> newNode = new Node<E>(e);
        //死循环
        for (; ; ) {
            //当前栈顶结点
            Node<E> curTopNode = top.get();
            //新结点的next指针指向原栈顶结点
            newNode.next = curTopNode;
            //CAS更新栈顶指针
            if (top.compareAndSet(curTopNode, newNode)) {
                return true;
            }
        }

    }


    /**
     *
     * @return 返回栈顶结点中的值,若栈为空返回null
     */
    public E pop() {

        //死循环
        for (; ; ) {
            //当前栈顶结点
            Node<E> curTopNode = top.get();
            //栈为空,返回null
            if (curTopNode == null) {
                return null;
            } else {
                //获得原栈顶结点的后继结点
                Node<E> nextNode = curTopNode.next;
                //CAS更新栈顶指针
                if (top.compareAndSet(curTopNode, nextNode)) {
                    return curTopNode.item;
                }
            }
        }
    }


    //定义链表结点
    private static class Node<E> {

        public E item;

        public Node<E> next;

        public Node(E item) {
            this.item = item;
        }
    }
}
