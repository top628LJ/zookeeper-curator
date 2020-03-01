package com.midworm.zookeeper.leader.latch;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class LeaderSelection {

    private static CuratorFramework client;

    static {
        client = CuratorFrameworkFactory.builder()
                .connectString("47.115.126.161:2181,47.115.125.138:2181,47.115.112.137:2181")
                .retryPolicy(new ExponentialBackoffRetry(2000, 3))
                .sessionTimeoutMs(15000)
                .connectionTimeoutMs(5000)
                .namespace("leaderLatch")
                .build();
        client.start();
    }

    private String nodeName;
    private LeaderLatch leaderLatch;

    public LeaderSelection(String lockPath, String nodeName) {
        this.nodeName = nodeName;
        initSelection(lockPath);
    }

    private void initSelection(String lockPath) {
        leaderLatch = new LeaderLatch(client, lockPath);
        try {
            leaderLatch.start();
            Thread.sleep(500);
            System.out.println(nodeName + " is leader: " + leaderLatch.hasLeadership());
        } catch (Exception e) {
            System.out.println("init selection failed");
            e.printStackTrace();
        }
    }

    public void cleanUp() throws Exception {
        leaderLatch.close();
    }

    public boolean isLeader() {
        return leaderLatch.hasLeadership();
    }
}
