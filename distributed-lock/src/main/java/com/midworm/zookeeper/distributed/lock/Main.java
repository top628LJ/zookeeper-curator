package com.midworm.zookeeper.distributed.lock;

import org.apache.curator.framework.recipes.locks.InterProcessLock;

import java.util.concurrent.CountDownLatch;

public class Main {
    public static int a = 0, b = 0;

    public static void outputA() {
        System.out.println("a is: " + a);
    }

    public static void addA() {
        a = a + 1;
    }

    public static void outputB() {
        System.out.println("b is: " + b);
    }

    public static void addB() {
        b = b + 1;
    }

    public static void main(String[] args) throws Exception {
        CountDownLatch countDownLatchI = new CountDownLatch(1);
        CountDownLatch countDownLatchJ = new CountDownLatch(1);

        for (int i = 0; i < 100; ++i) {
            new Thread(() -> {
                try {
                    countDownLatchI.await();
                    for (int j = 0; j < 1000; ++j) {
                        addA();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
        countDownLatchI.countDown();

        Thread.sleep(5000);

        for (int i = 0; i < 100; ++i) {
            new Thread(() -> {
                try {
                    countDownLatchJ.await();
                    InterProcessLock lock = DistributedLock.lock("lockPath");
                    for (int j = 0; j < 1000; ++j) {
                        addB();
                    }
                    lock.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
        countDownLatchJ.countDown();

        Thread.sleep(5000);
        outputA();
        outputB();
    }
}
