package com.scg.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 1/14/13
 * Time: 8:14 PM
 */
public class TestJunk {

    public static void main(String[] args) {
        Deadlock dl = new Deadlock();
        //Thread t = new Thread(new Deadlock());
        //t.start();

        List<Integer> list = new ArrayList<Integer>();
        Producer producer = new Producer(list);
        Consumer consumer = new Consumer(list);

        Thread producerThread = new Thread(producer);
        Thread consumerThread = new Thread(consumer);

        consumerThread.start();
        producerThread.start();
    }


    public class Foo {
        private int value;

        public Foo(final int value) {
            this.value = value;
        }
        public class Bar {
            public void process() {
                System.out.println(value);
            }
        }
    }

    private static class Deadlock implements Runnable{
        Object locka = new Object();
        Object lockb = new Object();

        public void method1() {
            synchronized (locka) {
                synchronized (lockb) {
                    System.out.println("method1");
                }
            }
        }

        public void method2() {
            synchronized (lockb) {
                synchronized (locka) {
                    System.out.println("method2");
                }
            }
        }


        public void run() {
            while (true) {
                method1();
                method2();
            }
        }

    }


    private static class Producer implements Runnable {
        private static final Logger LOG = Logger.getLogger(Producer.class.getName());

        private final List<Integer> list;
        private volatile int val = 0;

        private Producer(List<Integer> list) {
            this.list = list;
        }

        @Override
        public void run() {
            synchronized (list) {
                while (true) {
                    list.add(++val);
                    list.notify();
                    Thread.yield();
                }
            }
        }
    }

    private static class Consumer implements Runnable {
        private static final Logger LOG = Logger.getLogger(Consumer.class.getName());

        private final List<Integer> list;

        private Consumer(List<Integer> list) {
            this.list = list;
        }

        @Override
        public void run() {
            int count = 0;
            while (count < 1000) {
                synchronized (list) {
                    while (list.isEmpty()) {
                        try {
                            list.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    count = list.remove(list.size() - 1);
                    LOG.info("Value: " + count);
                    LOG.info(String.format("List is %d in size", list.size()));
                }
            }
        }
    }
}
