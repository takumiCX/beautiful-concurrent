package com.takumiCX.concurrent.queue;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author: takumiCX
 * @create: 2018-08-13
 * <p>
 * 单链表带哨兵结点的无锁队列
 **/
public class LockFreeSingleLinkedQueue<E> implements LockFreeQueue<E> {


    //不带任何信息的哨兵结点
    Node<E> sentinel = new Node<>(null);

    //头指针指向哨兵结点
    Node<E> head = sentinel;

    //尾指针,原子引用,初始化时指向哨兵结点
    AtomicReference<Node<E>> tail = new AtomicReference<>(sentinel);

    /**
     * 将元素加入队列尾部
     * @param e 要入队的元素
     * @return  true:入队成功 false:入队失败
     */
    @Override
    public boolean enqueue(E e) {

        //构造新结点
        Node<E> newNode = new Node<>(e);
        //死循环,保证入队
        for (; ; ) {

            //当前尾结点
            Node<E> tailed = tail.get();
            //当前尾结点的下一个结点
            Node<E> tailedNext = tailed.next;

            //判断队列此时正处于出队操作导致的中间状态
            if (sentinel.next == null && tailed != sentinel) {

                //CAS方式使尾指针指向哨兵结点,失败也没关系
                tail.compareAndSet(tailed, sentinel);

            } else if (tailed == tail.get()) {   //尾指针尚未改变,即没有其他线程将结点插入队列
                //其他线程正在执行入队,此时队列处于中间状态
                if (tailedNext != null) {

                    //替其他线程完成更新尾指针的操作
                    tail.compareAndSet(tailed, tailedNext);

                } else if (tailed.casNext(null, newNode)) {  //没有其他线程正在执行入队,CAS更新尾原结点的next指针指向新结点

                    //CAS更新尾指针,失败也没关系
                    tail.compareAndSet(tailed, newNode);
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
    @Override
    public E dequeue() {

        for (; ; ) {
            //队列首元素结点(哨兵结点之后的结点)
            Node<E> headed = head.next;

            //队列尾结点
            Node<E> tailed = tail.get();

            if (headed == null) {   //队列为空,返回null
                return null;

            } else if (headed == tailed) {   //队列中只含一个元素
                //CAS方式修改哨兵结点的next指针指向null
                if (sentinel.casNext(headed, null)) {
                    //cas方式修改尾指针指向哨兵结点,失败也没关系
                    tail.compareAndSet(tailed, sentinel);
                    return headed.item;
                }

            } else if (sentinel.casNext(headed, headed.next)) { //队列中元素个数大于1
                return headed.item;
            }
        }
    }

    //链表结点
    private static class Node<E> {

        //UNSAFE对象,用来进行CAS操作
        private static final sun.misc.Unsafe UNSAFE;
        //next指针域在Node对象中的偏移量
        private static final long nextOffset;

        static {
            try {

                //类加载时执行,反射方式创建UNSAFE对象,我们要通过该对象以CAS的方式更新
                //Node对象中的next指针
                Class<?> unsafeClass = Unsafe.class;
                Field f = unsafeClass.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                UNSAFE = (Unsafe) f.get(null);
//                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> k = LockFreeSingleLinkedQueue.Node.class;
                nextOffset = UNSAFE.objectFieldOffset
                        (k.getDeclaredField("next"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }

        //CAS方式更新next指针,expect:cmp update:val
        private boolean casNext(Node<E> cmp, Node<E> val) {
            return UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
        }


        //实际存储的元素
        public E item;

        //指向下一个结点的指针
        public volatile Node<E> next;

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
