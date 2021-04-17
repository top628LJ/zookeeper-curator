package com.midworm.zookeeper.distributed.lock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class DistributedLock {

    private static CuratorFramework client;

    static {
        client = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181")
                .sessionTimeoutMs(30000)
                .connectionTimeoutMs(5000)
                .retryPolicy(new ExponentialBackoffRetry(3000, 3))
                .namespace("distributedLock")
                .build();
        client.start();
    }

    public static InterProcessLock lock(String lockPath) {
        if (!lockPath.startsWith("/")) {
            lockPath = "/" + lockPath;
        }
        try {
            final InterProcessLock lock = new InterProcessMutex(client, lockPath);
            lock.acquire();
            System.out.println("lock success, lock path: " + lockPath);
            return lock;
        } catch (Exception e) {
            System.out.println("lock failed");
            e.printStackTrace();
        }
        return null;
    }

    public static void unlock(InterProcessLock lock) {
        if (lock == null) {
            return;
        }
        try {
            lock.release();
            System.out.println("unlock success");
        } catch (Exception e) {
            System.out.println("unlock failed");
            e.printStackTrace();
        }
    }
}
