package com.takumiCX.concurrent.queue;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author: takumiCX
 * @create: 2018-08-07
 * <p>
 * 基于双向链表和CAS算法实现的无锁的并发队列
 **/
public class LockFreeLinkedQueue<E> implements LockFreeQueue<E> {


    //指向队列头结点的原子引用
    private AtomicReference<Node<E>> head = new AtomicReference<>(null);

    //指向队列尾结点的原子引用
    private AtomicReference<Node<E>> tail = new AtomicReference<>(null);

    public LockFreeLinkedQueue() {

    }


    /**
     * 将元素加入队列尾部
     *
     * @param e 要入队的元素
     * @return true:入队成功 false:入队失败
     */
    public boolean enqueue(E e) {

        //创建一个包含入队元素的新结点
        Node<E> newNode = new Node<>(e);
        //死循环
        for (; ; ) {
            //记录当前尾结点
            Node<E> taild = tail.get();
            //当前尾结点为null,说明队列为空
            if (taild == null) {
                //CAS方式更新队列头指针
                if (head.compareAndSet(null, newNode)) {
                    //非同步方式更新尾指针
                    tail.set(newNode);
                    return true;
                }

            } else {

                //新结点的pre指针指向原尾结点
                newNode.pre = taild;
                //CAS方式将尾指针指向新的结点
                if (tail.compareAndSet(taild, newNode)) {
                    //非同步方式更新
                    taild.next = newNode;
                    return true;
                }
            }
        }
    }

    /**
     * 将队列首元素从队列中移除并返回该元素,若队列为空则返回null
     *
     * @return
     */
    public E dequeue() {

        //死循环
        for (; ; ) {
            //当前头结点
            Node<E> tailed = tail.get();
            //当前尾结点
            Node<E> headed = head.get();

            if (tailed == null) { //尾结点为null,说明队列为空,直接返回null
                return null;
            } else if (headed == tailed) { //尾结点和头结点相同,说明队列中只有一个元素,此时要更新头尾指针
                //CAS方式更新尾指针为null
                if (tail.compareAndSet(tailed, null)) {
                    //头指针更新为null
                    head.set(null);
                    return headed.item;
                }

            } else {
                //走到这一步说明队列中元素结点的个数大于1,只要更新队列头指针指向原头结点的下一个结点就行
                //但是要注意头结点的下一个结点可能为null,所以要先确保新的队列头结点不为null

                //队列头结点的下一个结点
                Node headedNext = headed.next;
                if (headedNext != null && head.compareAndSet(headed, headedNext))
                    headedNext.pre=null;  //help gc
                    return headed.item;
            }
        }
    }


    /**
     * 链表节点的定义
     * @param <E>
     */
    private static class Node<E> {

        //指向前一个节点的指针
        public volatile Node pre;

        //指向后一个结点的指针
        public volatile Node next;

        //真正要存储在队列中的值
        public E item;

        public Node(E item) {
            this.item = item;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "item=" + item +
                    '}';
        }
    }
}
